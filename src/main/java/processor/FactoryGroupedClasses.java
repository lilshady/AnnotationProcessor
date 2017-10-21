package processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: comment here
 */
public class FactoryGroupedClasses {

	private String qualifiedClassName;

	private static final String SUFFIX = "Factory";

	private Map<String, FactoryAnnotatedClass> itemsMap = new LinkedHashMap<>();

	public FactoryGroupedClasses(String qualifiedClassName) {
		this.qualifiedClassName = qualifiedClassName;
	}

	public void add(FactoryAnnotatedClass toInsert) {

		FactoryAnnotatedClass existing = itemsMap.get(toInsert.getId());

		if (existing == null) {
			itemsMap.put(toInsert.getId(), toInsert);

		}

	}

	public void generateCode(Elements elementsUtils, Filer filer) throws IOException {

		TypeElement superClassName = elementsUtils.getTypeElement(qualifiedClassName);
		String factoryClassName = superClassName.getSimpleName() + SUFFIX;
		PackageElement pkg = elementsUtils.getPackageOf(superClassName);
		String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();

		MethodSpec.Builder method = MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(String.class, "id")
				.returns(TypeName.get(superClassName.asType()));

		// check if id is null
		method.beginControlFlow("if (id == null)")
				.addStatement("throw new IllegalArgumentException($S)", "id is null!")
				.endControlFlow();

		// Generate items map

		for (FactoryAnnotatedClass item : itemsMap.values()) {
			method.beginControlFlow("if ($S.equals(id))", item.getId())
					.addStatement("return new $L()", item.getTypeElement().getQualifiedName().toString())
					.endControlFlow();
		}

		method.addStatement("throw new IllegalArgumentException($S + id)", "Unknown id = ");

		TypeSpec typeSpec = TypeSpec.classBuilder(factoryClassName).addMethod(method.build()).build();

		// Write file
		JavaFile.builder(packageName, typeSpec).build().writeTo(filer);

	}
}
