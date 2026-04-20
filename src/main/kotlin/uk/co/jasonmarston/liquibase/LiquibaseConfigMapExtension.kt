package uk.co.jasonmarston.liquibase

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class LiquibaseConfigMapExtension @Inject constructor(objects: ObjectFactory) {
    abstract val changelogDir: DirectoryProperty
    abstract val configMapName: Property<String>
    abstract val warnKb: Property<Long>
    abstract val failKb: Property<Long>
    abstract val kubectlContext: Property<String>
    abstract val kubectlNamespace: Property<String>
}
