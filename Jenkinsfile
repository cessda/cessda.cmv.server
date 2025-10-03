pipeline {

    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '20')
		disableConcurrentBuilds abortPrevious: true
    }

    environment {
        productName = "cmv"
        componentName = "server"
		IMAGE_TAG = "${GIT_COMMIT}-${env.BUILD_NUMBER}"
        DESTINATION_IMAGE = "${DOCKER_ARTIFACT_REGISTRY}/${productName}-${componentName}:${IMAGE_TAG}"
    }

    agent {
        label 'jnlp-himem'
    }

    stages {
        // Building on master
        stage('Pull SDK Docker Image') {
			environment {
				HOME = "${WORKSPACE_TMP}"
			}
            agent {
                docker {
					image 'eclipse-temurin:21'
                    reuseNode true
                }
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean verify'
                        }
                    }
                }
                stage('Record Issues') {
                    steps {
                        recordIssues aggregatingResults: true, tools: [java()]
                    }
                }
                stage('Run SonarQube Scan') {
                    steps {
                        withSonarQubeEnv('cessda-sonar') {
                            withMaven {
                                sh './mvnw sonar:sonar'
                            }
                        }
                        timeout(time: 1, unit: 'HOURS') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                    when { branch 'main' }
                }
            }
        }
		stage('Build Docker image') {
			steps {
				sh "docker build -t ${DESTINATION_IMAGE} ."
			}
		}
		stage('Push Docker image') {
			steps {
				sh "gcloud auth configure-docker ${ARTIFACT_REGISTRY_HOST}"
				sh "docker push ${DESTINATION_IMAGE}"
			}
			when { branch 'main' }
		}
        stage('Deploy CMV Server') {
            steps {
                build job: 'cessda.cmv.deploy/main', parameters: [string(name: 'serverImageTag', value: "${IMAGE_TAG}")], wait: false
            }
            when { branch 'main' }
        }
    }
}
