/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2024 CESSDA ERIC
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
import org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

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
		// Documentation handler.
		registry.addResourceHandler( "/documentation/**" )
				.addResourceLocations( "classpath:/cmv-documentation/" )
				.resourceChain( false )
				.addResolver( pathResourceResolver );

		// Static content resource handler.
		registry.addResourceHandler( "/**" )
				.addResourceLocations( "classpath:/static/" )
				.resourceChain( false )
				.addResolver( pathResourceResolver );
	}

	@Override
	public void addViewControllers( ViewControllerRegistry registry )
	{
		// Documentation redirects
		registry.addRedirectViewController( "/documentation", "/documentation/index.html" );
		registry.addRedirectViewController( "/documentation/", "/documentation" );

		// CDC profile redirects
		registry.addRedirectViewController( "/profiles/cdc/ddi-1.2.2/latest/profile.xml", "/profiles/cdc/ddi-1.2.2/2.0.0/profile.xml" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-1.2.2/latest/profile.html", "/profiles/cdc/ddi-1.2.2/2.0.0/profile.html" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-1.2.2/latest/profile-mono.xml", "/profiles/cdc/ddi-1.2.2/2.0.0/profile-mono.xml" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-1.2.2/latest/profile-mono.html", "/profiles/cdc/ddi-1.2.2/2.0.0/profile-mono.html" );

		registry.addRedirectViewController( "/profiles/cdc/ddi-2.5/latest/profile.xml", "/profiles/cdc/ddi-2.5/2.0.0/profile.xml" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-2.5/latest/profile.html", "/profiles/cdc/ddi-2.5/2.0.0/profile.html" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-2.5/latest/profile-mono.xml", "/profiles/cdc/ddi-2.5/2.0.0/profile-mono.xml" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-2.5/latest/profile-mono.html", "/profiles/cdc/ddi-2.5/2.0.0/profile-mono.html" );

		registry.addRedirectViewController( "/profiles/cdc/ddi-3.2/latest/profile.xml", "/profiles/cdc/ddi-3.2/1.0.0/profile.xml" );
		registry.addRedirectViewController( "/profiles/cdc/ddi-3.2/latest/profile.html", "/profiles/cdc/ddi-3.2/1.0.0/profile.html" );

		// EQB profile redirects
		registry.addRedirectViewController( "/profiles/eqb/ddi-2.5/latest/profile.xml", "/profiles/eqb/ddi-2.5/0.1.0/profile.xml" );
		registry.addRedirectViewController( "/profiles/eqb/ddi-2.5/latest/profile.html", "/profiles/eqb/ddi-2.5/0.1.0/profile.html" );
		registry.addRedirectViewController( "/profiles/eqb/ddi-3.2/latest/profile.xml", "/profiles/eqb/ddi-3.2/0.1.1/profile.xml" );
		registry.addRedirectViewController( "/profiles/eqb/ddi-3.2/latest/profile.html", "/profiles/eqb/ddi-3.2/0.1.1/profile.html" );
	}

	@Override
	public void extendHandlerExceptionResolvers( List<HandlerExceptionResolver> resolvers )
	{
		ExceptionHandlerExceptionResolver defaultResolver = (ExceptionHandlerExceptionResolver) resolvers.stream()
				.filter( ExceptionHandlerExceptionResolver.class::isInstance ).findAny()
				.orElseThrow( () -> new IllegalStateException(
						"No registered " + ExceptionHandlerExceptionResolver.class.getSimpleName() + " found." ) );
		var resolver = new ResourceExceptionHandlerExceptionResolver();
		resolver.setApplicationContext( defaultResolver.getApplicationContext() );
		resolver.setContentNegotiationManager( defaultResolver.getContentNegotiationManager() );
		resolver.setCustomArgumentResolvers( defaultResolver.getCustomArgumentResolvers() );
		resolver.setCustomReturnValueHandlers( defaultResolver.getCustomReturnValueHandlers() );
		resolver.afterPropertiesSet();
		resolver.setReturnValueHandlers( Objects.requireNonNull( defaultResolver.getReturnValueHandlers() )
				.getHandlers() );
		resolver.setArgumentResolvers( Objects.requireNonNull( defaultResolver.getArgumentResolvers() )
				.getResolvers() );
		resolvers.add( resolver );
	}

	private static class ResourceExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver
	{
		/**
		 * Resolve exceptions that come from an instance of {@link ResourceHttpRequestHandler} by delegating to
		 * {@link AbstractHandlerMethodExceptionResolver#doResolveException(HttpServletRequest, HttpServletResponse, Object, Exception)}.
		 */
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
}
