package uk.co.jasonmarston.versions

data class KiunziVersions(
  val javaVersion: Int,
  val javaSourceVersion: Int,
  val javaByteCodeVersion: Int,
  val junitVersion: String,
  val modelMapperVersion: String,
  val modelMapperModuleRecordVersion: String,
  val quarkusPlatformVersion: String
)
