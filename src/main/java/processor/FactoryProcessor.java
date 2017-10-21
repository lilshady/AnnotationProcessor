package processor;

import annotation.Factory;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.xml.transform.Source;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: comment here
 */
@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager;
	private Map<String, FactoryGroupedClasses> factoryGroupedClasses = new LinkedHashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new LinkedHashSet<String>();
		annotations.add(Factory.class.getCanonicalName());
		return annotations;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return super.getCompletions(element, annotation, member, userText);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Factory.class)) {

			if (annotatedElement.getKind() != ElementKind.CLASS) {
				messager.printMessage(Diagnostic.Kind.ERROR, "fucked",annotatedElement);
				return true;
			}

			TypeElement typeElement = (TypeElement) annotatedElement;

			try {
				FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement);

				if (!valid(annotatedClass)) {
					return true;
				}

				FactoryGroupedClasses factoryGroupedClass =  factoryGroupedClasses.computeIfAbsent(annotatedClass.getQualifiedSuperClassName(), s -> new FactoryGroupedClasses(s));

				factoryGroupedClass.add(annotatedClass);

				try {
					for (FactoryGroupedClasses factoryGroupedClasses : factoryGroupedClasses.values()) {
						factoryGroupedClasses.generateCode(elementUtils, filer);
					}

					factoryGroupedClasses.clear();
				} catch (IOException e) {
					messager.printMessage(Diagnostic.Kind.ERROR, "fucked",annotatedElement);
				}

			} catch (IllegalArgumentException e) {
				messager.printMessage(Diagnostic.Kind.ERROR, "fucked",annotatedElement);
				return true;
			}
		}

		return true;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	private boolean valid(FactoryAnnotatedClass annotatedClass) {
		return true;
	}
}
