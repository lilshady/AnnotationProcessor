package processor;

import annotation.Factory;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

/**
 * TODO: comment here
 */
public class FactoryAnnotatedClass {
	private TypeElement annotatedClassElement;
	private String qualifiedSuperClassName;
	private String simpleTypeName;
	private String id;

	public FactoryAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {
		this.annotatedClassElement = classElement;
		Factory annotation = classElement.getAnnotation(Factory.class);
		id = annotation.id();

		if (StringUtils.isEmpty(id)) {
			throw  new IllegalArgumentException("fucked");
		}

		try {
			Class<?> clazz = annotation.type();
			qualifiedSuperClassName = clazz.getCanonicalName();
			simpleTypeName = clazz.getSimpleName();
		} catch (MirroredTypeException mte) {
			DeclaredType declaredType = (DeclaredType) mte.getTypeMirror();
			TypeElement typeElement = (TypeElement) declaredType.asElement();
			qualifiedSuperClassName = typeElement.getQualifiedName().toString();
			simpleTypeName = typeElement.getSimpleName().toString();
		}

	}

	public String getId() {
		return id;
	}

	public String getQualifiedSuperClassName() {
		return qualifiedSuperClassName;
	}

	public String getSimpleTypeName() {
		return simpleTypeName;
	}

	public TypeElement getTypeElement() {
		return annotatedClassElement;
	}
}