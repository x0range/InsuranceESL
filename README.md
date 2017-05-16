# InsuranceESL
Insurance modelling framework based on the EconomicSL

# How to build

InsuranceESL builds with Maven and requires org.economicsl (i.e., EconomicSL). If you do not handle Maven through your IDE, follow these steps:

1. Download EconomicSL (either as jar or as source and build into jar) and install in Maven locally (replace the the path to the jar in the -Dfile argument):

    mvn install:install-file -Dfile=/path/to/EconomicSL/jar/economicsl-1.0-SNAPSHOT.jar -DgroupId=org -DartifactId=economicsl -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

2. Compile InsuranceESL (ensure that your current working directory is the directory of the InsuranceESL source package):

    mvn clean install -U

3. Run InsuranceESL

    mvn exec:java -X -D exec.mainClass=insuranceesl.insurance.Insurance
