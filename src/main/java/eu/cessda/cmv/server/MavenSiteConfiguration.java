package eu.cessda.cmv.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceResolverChain;

@Configuration
public class MavenSiteConfiguration implements WebMvcConfigurer
{
	@Override
	public void addResourceHandlers( ResourceHandlerRegistry registry )
	{
		registry.addResourceHandler( "/**" )
				.addResourceLocations( "classpath:/eu.cessda.cmv/cmv/" )
				.resourceChain( false )
				.addResolver( new PathResourceResolver()
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
				} );
	}

	@Override
	public void addViewControllers( ViewControllerRegistry registry )
	{
		registry.addRedirectViewController( "/", "/index.html" );
	}

	@Override
	public void extendHandlerExceptionResolvers( List<HandlerExceptionResolver> resolvers )
	{
		ExceptionHandlerExceptionResolver defaultResolver = (ExceptionHandlerExceptionResolver) resolvers.stream()
				.filter( resolver -> resolver instanceof ExceptionHandlerExceptionResolver ).findAny()
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
					return doResolveException( request, response, (HandlerMethod) null, ex );
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
