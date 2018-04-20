/*
 * Copyright Apehat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apehat.event.complier;

import com.apehat.event.annotation.Subscribe;
import com.apehat.event.annotation.Subscribes;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@code Subscribe} and {@code Subscribers} annotation processor.
 * <p>
 * This processor will process annotation and onEvent at Java compiler. And
 * will change byte code.
 *
 * @author hanpengfei
 * @since 1.0
 */
public final class SubscribeProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    private void error(Element element, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("Only element can be annotated with @%s", args), element);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<AnnotatedMethod> methods = new HashSet<>();

        methods.addAll(subscribersAnnotatedMethods(roundEnv));
        methods.addAll(subscriberAnnotatedMethods(roundEnv));
        processSubscribe(methods);
        return true;
    }

    private Set<AnnotatedMethod> subscriberAnnotatedMethods(RoundEnvironment roundEnv) {
        Set<AnnotatedMethod> methods = new HashSet<>();
        // the elements what had be annotated by Subscriber
        Set<? extends Element> subAnnotationMethods = roundEnv.getElementsAnnotatedWith(Subscribe.class);
        for (Element element : subAnnotationMethods) {
            Subscribe subscribe = element.getAnnotation(Subscribe.class);
            try {
                AnnotatedMethod method = new AnnotatedMethod(subscribe, element);
                methods.add(method);
            } catch (IllegalArgumentException e) {
                error(element, Subscribe.class);
                throw e;
            }
        }
        return methods;
    }

    private Set<AnnotatedMethod> subscribersAnnotatedMethods(RoundEnvironment roundEnv) {
        Set<AnnotatedMethod> methods = new HashSet<>();
        // the elements what had be annotated by Subscribers
        Set<? extends Element> subsAnnotationMethods = roundEnv.getElementsAnnotatedWith(Subscribes.class);
        Element currentElement = null;
        try {
            for (Element element : subsAnnotationMethods) {
                currentElement = element;
                Subscribes subscribes = element.getAnnotation(Subscribes.class);
                Subscribe[] subs = subscribes.value();
                for (Subscribe subscribe : subs) {
                    AnnotatedMethod method = new AnnotatedMethod(subscribe, element);
                    methods.add(method);
                }
            }
        } catch (IllegalArgumentException e) {
            error(currentElement, Subscribes.class);
            throw e;
        }
        return methods;
    }

    private void processSubscribe(Set<AnnotatedMethod> methods) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Process " + methods.toString());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new HashSet<>();
        names.add(Subscribe.class.getName());
        names.add(Subscribes.class.getName());
        return Collections.unmodifiableSet(names);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private static class AnnotatedMethod {

        private final Subscribe annotation;
        private final ExecutableElement element;

        private AnnotatedMethod(Subscribe annotation, Element element) {
            assert annotation != null;
            assert element != null;

            if (element.getKind() != ElementKind.METHOD) {
                throw new IllegalArgumentException();
            }

            this.annotation = annotation;
            this.element = (ExecutableElement) element;
        }

        /**
         * Determine whether the element is a normal class member, or is an
         * abstract class member and isn't abstract.
         *
         * @return true, the element is belong to a normal class, or abstract
         * class and isn't abstract.
         */
        private boolean isNormalMethod() {
            Set<Modifier> modifiers = element.getModifiers();
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof AnnotatedMethod)) {
                return false;
            }
            AnnotatedMethod method = (AnnotatedMethod) o;
            return annotation.equals(method.annotation) && element.equals(method.element);
        }

        @Override
        public int hashCode() {
            int hash = 203;
            hash += 31 * hash + annotation.hashCode();
            hash += 31 * hash + element.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "AnnotatedMethod{" + "annotation=" + annotation + ", element=" + element + '}';
        }
    }
}
