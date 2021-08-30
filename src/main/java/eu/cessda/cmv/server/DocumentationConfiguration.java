/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2021 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.cessda.cmv.server;

import eu.cessda.cmv.server.api.ResourceNotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
public class DocumentationConfiguration implements WebMvcConfigurer
{
	@Override
	public void addResourceHandlers( ResourceHandlerRegistry registry )
	{
		PathResourceResolver pathResourceResolver = new PathResourceResolver()
		{
			// https://github.com/spring-projects/spring-framework/issues/22619#issue-423373838
			@Override
			public Resource resolveResource(
					HttpServletRequest request,
					String requestPath,
					List<? extends Resource> locations,
					ResourceResolverChain chain )
			{
				Resource resource = super.resolveResource( request, requestPath, locations, chain );
				if ( resource == null )
				{
					throw new ResourceNotFoundException( request.getMethod(), requestPath );
				}
				return resource;
			}
		};
		registry.addResourceHandler( "/documentation/**" )
				.addResourceLocations( "classpath:/cmv-documentation/" )
				.resourceChain( false )
				.addResolver( pathResourceResolver );
		registry.addResourceHandler( "/**" )
				.addResourceLocations("classpath:/static/")
				.resourceChain( false )
				.addResolver( pathResourceResolver );
	}

	@Override
	public void addViewControllers( ViewControllerRegistry registry )
	{
		registry.addRedirectViewController( "/documentation", "/documentation/index.html" );
		registry.addRedirectViewController( "/documentation/", "/documentation" );
	}

	@Override
	public void extendHandlerExceptionResolvers( List<HandlerExceptionResolver> resolvers )
	{
		ExceptionHandlerExceptionResolver defaultResolver = (ExceptionHandlerExceptionResolver) resolvers.stream()
				.filter( ExceptionHandlerExceptionResolver.class::isInstance ).findAny()
				.orElseThrow( () -> new IllegalStateException(
						"No registered " + ExceptionHandlerExceptionResolver.class.getSimpleName() + " found." ) );

		class ResourceExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver
		{
			@Override
			public ModelAndView resolveException(
					HttpServletRequest request,
					HttpServletResponse response,
					Object handler,
					Exception ex )
			{
				if ( handler instanceof ResourceHttpRequestHandler )
				{
					return doResolveException( request, response, null, ex );
				}
				return null;
			}
		}
		ExceptionHandlerExceptionResolver resolver = new ResourceExceptionHandlerExceptionResolver();
		resolver.setApplicationContext( defaultResolver.getApplicationContext() );
		resolver.setContentNegotiationManager( defaultResolver.getContentNegotiationManager() );
		resolver.setCustomArgumentResolvers( defaultResolver.getCustomArgumentResolvers() );
		resolver.setCustomReturnValueHandlers( defaultResolver.getCustomReturnValueHandlers() );
		resolver.afterPropertiesSet();
		resolver.setReturnValueHandlers( defaultResolver.getReturnValueHandlers() == null ? null
				: defaultResolver.getReturnValueHandlers().getHandlers() );
		resolver.setArgumentResolvers( defaultResolver.getArgumentResolvers() == null ? null
				: defaultResolver.getArgumentResolvers().getResolvers() );
		resolvers.add( resolver );
	}
}
