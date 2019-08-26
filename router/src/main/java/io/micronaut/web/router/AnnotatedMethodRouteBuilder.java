/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.web.router;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.reflect.ClassLoadingReporter;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Responsible for building {@link Route} instances for the annotations found in the {@code io.micronaut.http.annotation}
 * package.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Singleton
public class AnnotatedMethodRouteBuilder extends DefaultRouteBuilder implements ExecutableMethodProcessor<Controller> {

    private static final MediaType[] DEFAULT_MEDIA_TYPES = {MediaType.APPLICATION_JSON_TYPE};
    private final Map<Class, BiConsumer<BeanDefinition, ExecutableMethod>> httpMethodsHandlers = new LinkedHashMap<>();

    /**
     * @param executionHandleLocator The execution handler locator
     * @param uriNamingStrategy The URI naming strategy
     * @param conversionService The conversion service
     */
    public AnnotatedMethodRouteBuilder(ExecutionHandleLocator executionHandleLocator, UriNamingStrategy uriNamingStrategy, ConversionService<?> conversionService) {
        super(executionHandleLocator, uriNamingStrategy, conversionService);
        httpMethodsHandlers.put(Get.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Get.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                MediaType[] produces = resolveProduces(method, annotation);
                Route route = GET(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method).produces(produces);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
                if (annotation.booleanValue("headRoute").orElse(true)) {
                    route = HEAD(resolveUri(bean, uri,
                            method,
                            uriNamingStrategy),
                            bean,
                            method).produces(produces);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Created Route: {}", route);
                    }
                }
            });
        });

        httpMethodsHandlers.put(Post.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Post.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                MediaType[] consumes = resolveConsumes(method, annotation);
                MediaType[] produces = resolveProduces(method, annotation);
                Route route = POST(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                route = route.consumes(consumes).produces(produces);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });

        });

        httpMethodsHandlers.put(Put.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Put.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                MediaType[] consumes = resolveConsumes(method, annotation);
                MediaType[] produces = resolveProduces(method, annotation);
                Route route = PUT(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                route = route.consumes(consumes).produces(produces);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });
        });

        httpMethodsHandlers.put(Patch.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Patch.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                MediaType[] consumes = resolveConsumes(method, annotation);
                MediaType[] produces = resolveProduces(method, annotation);
                Route route = PATCH(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                route = route.consumes(consumes).produces(produces);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });
        });

        httpMethodsHandlers.put(Delete.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Delete.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                MediaType[] consumes = resolveConsumes(method, annotation);
                MediaType[] produces = resolveProduces(method, annotation);
                Route route = DELETE(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                route = route.consumes(consumes).produces(produces);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });
        });


        httpMethodsHandlers.put(Head.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Head.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                Route route = HEAD(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });
        });

        httpMethodsHandlers.put(Options.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Options.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                MediaType[] consumes = resolveConsumes(method, annotation);
                MediaType[] produces = resolveProduces(method, annotation);
                Route route = OPTIONS(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                route = route.consumes(consumes).produces(produces);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });
        });

        httpMethodsHandlers.put(Trace.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Trace.class).forEach(annotation -> {
                String uri = annotation.stringValue().orElse(UriMapping.DEFAULT_URI);
                Route route = TRACE(resolveUri(bean, uri,
                        method,
                        uriNamingStrategy),
                        bean,
                        method);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created Route: {}", route);
                }
            });
        });

        httpMethodsHandlers.put(Error.class, (BeanDefinition bean, ExecutableMethod method) -> {
            method.getAnnotationValuesByType(Error.class).forEach(annotation -> {
                boolean isGlobal = annotation.isTrue("global");
                Class declaringType = bean.getBeanType();
                if (annotation.isPresent("status")) {
                    Optional<HttpStatus> value = annotation.enumValue("status", HttpStatus.class);
                    value.ifPresent(httpStatus -> {
                        if (isGlobal) {
                            status(httpStatus, declaringType, method.getMethodName(), method.getArgumentTypes());
                        } else {
                            status(declaringType, httpStatus, declaringType, method.getMethodName(), method.getArgumentTypes());
                        }
                    });
                } else {
                    Class exceptionType = null;
                    if (annotation.isPresent(AnnotationMetadata.VALUE_MEMBER)) {
                        Optional<Class<?>> annotationValue = annotation.classValue();
                        if (annotationValue.isPresent()) {
                            if (Throwable.class.isAssignableFrom(annotationValue.get())) {
                                exceptionType = annotationValue.get();
                            }
                        }
                    }
                    if (exceptionType == null) {
                        exceptionType = Arrays.stream(method.getArgumentTypes())
                                .filter(Throwable.class::isAssignableFrom)
                                .findFirst()
                                .orElse(Throwable.class);
                    }

                    if (isGlobal) {
                        //noinspection unchecked
                        error(exceptionType, declaringType, method.getMethodName(), method.getArgumentTypes());
                    } else {
                        //noinspection unchecked
                        error(declaringType, exceptionType, declaringType, method.getMethodName(), method.getArgumentTypes());
                    }
                }
            });
        });
    }

    private MediaType[] resolveConsumes(ExecutableMethod method, AnnotationValue annotationValue) {
        String[] consumeStrings = annotationValue.stringValues("consumes");
        if (consumeStrings.length == 0) {
            consumeStrings = method.stringValues(Consumes.class);
        }
        MediaType[] consumes = MediaType.of(consumeStrings);
        if (ArrayUtils.isEmpty(consumes)) {
            consumes = DEFAULT_MEDIA_TYPES;
        }
        return consumes;
    }

    private MediaType[] resolveProduces(ExecutableMethod method, AnnotationValue annotationValue) {
        String[] produceStrings = annotationValue.stringValues("produces");
        if (produceStrings.length == 0) {
            produceStrings = method.stringValues(Produces.class);
        }
        MediaType[] produces = MediaType.of(produceStrings);
        if (ArrayUtils.isEmpty(produces)) {
            produces = DEFAULT_MEDIA_TYPES;
        }
        return produces;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        List<Class<? extends Annotation>> actionAnnotations = method.getAnnotationTypesByStereotype(HttpMethodMapping.class);
        for (Class<? extends Annotation> annotationClass : actionAnnotations) {
            BiConsumer<BeanDefinition, ExecutableMethod> handler = httpMethodsHandlers.get(annotationClass);
            if (handler != null) {
                ClassLoadingReporter.reportBeanPresent(method.getReturnType().getType());
                for (Class argumentType : method.getArgumentTypes()) {
                    ClassLoadingReporter.reportBeanPresent(argumentType);
                }
                handler.accept(beanDefinition, method);
            }
        }

        if (actionAnnotations.isEmpty() && method.isDeclaredAnnotationPresent(UriMapping.class)) {
            String uri = method.stringValue(UriMapping.class).orElse(UriMapping.DEFAULT_URI);
            MediaType[] produces = MediaType.of(method.stringValues(Produces.class));
            Route route = GET(resolveUri(beanDefinition, uri,
                    method,
                    uriNamingStrategy),
                    method.getDeclaringType(),
                    method.getMethodName(),
                    method.getArgumentTypes()).produces(produces);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created Route: {}", route);
            }
        }
    }

    private String resolveUri(BeanDefinition bean, String value, ExecutableMethod method, UriNamingStrategy uriNamingStrategy) {
        UriTemplate rootUri = UriTemplate.of(uriNamingStrategy.resolveUri(bean));
        if (StringUtils.isNotEmpty(value)) {
            return rootUri.nest(value).toString();
        } else {
            return rootUri.nest(uriNamingStrategy.resolveUri(method.getMethodName())).toString();
        }
    }
}
