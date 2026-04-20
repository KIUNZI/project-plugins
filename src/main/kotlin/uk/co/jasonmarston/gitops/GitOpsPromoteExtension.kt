package uk.co.jasonmarston.gitops

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GitOpsPromoteExtension {
    abstract val gitOpsRepoDir: DirectoryProperty
    abstract val appPath: Property<String>
    abstract val devValuesPath: Property<String>
    abstract val preProdValuesPath: Property<String>
    abstract val prodValuesPath: Property<String>
    abstract val imageTagKey: Property<String>
    abstract val imageDigestKey: Property<String>
    abstract val snapshotDigestFile: Property<String>
    abstract val gitPush: Property<Boolean>
}