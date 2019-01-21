//"Jenkins Pipeline is a suite of plugins which supports implementing and integrating continuous delivery pipelines into Jenkins. Pipeline provides an extensible set of tools for modeling delivery pipelines "as code" via the Pipeline DSL."
//More information can be found on the Jenkins Documentation page https://jenkins.io/doc/
pipeline {
    agent { label 'linux-small' }
    options {
        buildDiscarder(logRotator(numToKeepStr:'25'))
        disableConcurrentBuilds()
        timestamps()
    }
    triggers {
        /*
          Restrict nightly builds to master branch, all others will be built on change only.
          Note: The BRANCH_NAME will only work with a multi-branch job using the github-branch-source
        */
        cron(BRANCH_NAME == "master" ? "H H(21-23) * * *" : "")
    }
    environment {
        LINUX_MVN_RANDOM = '-Djava.security.egd=file:/dev/./urandom'
        COVERAGE_EXCLUSIONS = '**/test/**/*,**/itests/**/*,**/*Test*,**/sdk/**/*,**/*.js,**/node_modules/**/*,**/jaxb/**/*,**/wsdl/**/*,**/nces/sws/**/*,**/*.adoc,**/*.txt,**/*.xml'
        DISABLE_DOWNLOAD_PROGRESS_OPTS = '-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn '
    }
    stages {
        // The incremental build will be triggered only for PRs. It will build the differences between the PR and the target branch
        stage('Incremental Build') {
            when {
                allOf {
                    expression { env.CHANGE_ID != null }
                    expression { env.CHANGE_TARGET != null }
                }
            }
            parallel {
                stage ('Linux') {
                    steps {
                        // TODO: Maven downgraded to work around a linux build issue. Falling back to system java to work around a linux build issue. re-investigate upgrading later
                        withMaven(maven: 'Maven 3.5.3', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LINUX_MVN_RANDOM}') {
                            sh 'mvn install -B -DskipStatic=true -DskipTests=true $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                            sh 'mvn install -B -Dgib.enabled=true -Dgib.referenceBranch=/refs/remotes/origin/$CHANGE_TARGET $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                        }
                    }
                }
                stage ('Windows') {
                    agent { label 'server-2016-small' }
                    steps {
                        withMaven(maven: 'Maven 3.5.3', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings') {
                            bat 'mvn install -B -DskipStatic=true -DskipTests=true  %DISABLE_DOWNLOAD_PROGRESS_OPTS%'
                            bat 'mvn install -B -Dgib.enabled=true -Dgib.referenceBranch=/refs/remotes/origin/%CHANGE_TARGET% %DISABLE_DOWNLOAD_PROGRESS_OPTS%'
                        }
                    }
                }
            }
        }
        stage('Full Build') {
            when { expression { env.CHANGE_ID == null } }
            parallel {
                stage ('Linux') {
                    steps {
                        // TODO: Maven downgraded to work around a linux build issue. Falling back to system java to work around a linux build issue. re-investigate upgrading later
                        withMaven(maven: 'Maven 3.5.3', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LINUX_MVN_RANDOM}') {
                            sh 'mvn clean install -B $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                        }
                    }
                }
                stage ('Windows') {
                    agent { label 'server-2016-small' }
                    steps {
                        withMaven(maven: 'Maven 3.5.3', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings') {
                            bat 'mvn clean install -B %DISABLE_DOWNLOAD_PROGRESS_OPTS%'
                        }
                    }
                }
            }
        }
        stage('Security Analysis') {
            parallel {
                stage ('Owasp') {
                    steps {
                        withMaven(maven: 'Maven 3.5.3', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LINUX_MVN_RANDOM}') {
                            // If this build is not a pull request, run full owasp scan. Otherwise run incremental scan
                            script {
                                if (env.CHANGE_ID == null) {
                                    sh 'mvn install -B -Powasp -DskipTests=true -nsu $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                                } else {
                                    sh 'mvn install -B -Powasp -DskipTests=true  -Dgib.enabled=true -Dgib.referenceBranch=/refs/remotes/origin/$CHANGE_TARGET -nsu $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('Quality Analysis') {
            parallel {
                // Sonar stage only runs against master
                stage ('SonarCloud') {
                    when {
                        allOf {
                            expression { env.CHANGE_ID == null }
                            branch 'master'
                            environment name: 'JENKINS_ENV', value: 'prod'
                        }
                    }
                    steps {
                        withMaven(maven: 'M35', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LARGE_MVN_OPTS} ${LINUX_MVN_RANDOM}') {
                            withCredentials([string(credentialsId: 'SonarQubeGithubToken', variable: 'SONARQUBE_GITHUB_TOKEN'), string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                                script {
                                    sh 'mvn -q -B -Dcheckstyle.skip=true org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN  -Dsonar.organization=cx -Dsonar.projectKey=replication -Dsonar.exclusions=${COVERAGE_EXCLUSIONS} -pl !$DOCS,!$ITESTS $DISABLE_DOWNLOAD_PROGRESS_OPTS'

                                 }
                            }
                        }
                    }
                }
            }
        }
        /*
         Deploy stage will only be executed for deployable branches. These include master and any patch branch matching M.m.x format (i.e. 2.10.x, 2.9.x, etc...).
         It will also only deploy in the presence of an environment variable JENKINS_ENV = 'prod'. This can be passed in globally from the jenkins master node settings.
        */
        stage('Deploy') {
            when {
                allOf {
                    expression { env.CHANGE_ID == null }
                    expression { env.BRANCH_NAME ==~ /((?:\d*\.)?\d.x|master)/ }
                    environment name: 'JENKINS_ENV', value: 'prod'
                }
            }
            steps{
                withMaven(maven: 'Maven 3.5.3', jdk: 'jdk8-latest', globalMavenSettingsConfig: 'default-global-settings', mavenSettingsConfig: 'codice-maven-settings', mavenOpts: '${LINUX_MVN_RANDOM}') {
                    sh 'mvn javadoc:aggregate -B -DskipStatic=true -DskipTests=true -nsu $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                    sh 'mvn deploy -B -DskipStatic=true -DskipTests=true -DretryFailedDeploymentCount=10 -nsu $DISABLE_DOWNLOAD_PROGRESS_OPTS'
                }
            }
        }
    }

}
