pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'Java21'
    }

    triggers {
        // Poll GitHub exactly every 5 minutes for changes
        pollSCM('*/5 * * * *')

        // Alternatively, use cron for scheduled builds regardless of changes:
        // cron('H 9,17 * * 1-5')  // Run at 9 AM and 5 PM on weekdays
    }

    parameters {
        choice(name: 'BROWSER', choices: ['Chrome', 'Firefox'], description: 'Select browser')
        choice(name: 'ENV', choices: ['QA', 'DEV', 'PROD'], description: 'Select environment')
    }

    environment {
        // AWS Config
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'qa-accelerator-inadev'
        S3_PREFIX = 'all_reports'

        // Maven memory
        MAVEN_OPTS = '-Xmx1024m'

        // Pipeline URL for reports
        PIPELINE_URL = "${BUILD_URL}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo '🔍 Checking out code from GitHub...'
                checkout scm
                script {
                    env.BUILD_TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
                }
            }
        }

        stage('Install Playwright') {
            steps {
                echo '🎭 Installing Playwright...'
                script {
                    if (isUnix()) {
                        sh 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
                    } else {
                        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"'
                    }
                }
            }
        }

        // ✅ UPDATED PART (Continue even if tests fail)
        stage('Run Tests') {
            steps {
                echo "🧪 Running Tests on ${params.BROWSER} in ${params.ENV}"
                script {
                    def status
                    if (isUnix()) {
                        status = sh(script: "mvn clean verify -Dbrowser=${params.BROWSER} -Denv=${params.ENV} -Dmaven.test.failure.ignore=true", returnStatus: true)
                    } else {
                        status = bat(script: "mvn clean verify -Dbrowser=${params.BROWSER} -Denv=${params.ENV} -Dmaven.test.failure.ignore=true", returnStatus: true)
                    }

                    if (status != 0) {
                        currentBuild.result = 'FAILURE'
                        echo "❌ Tests failed! Continuing pipeline..."
                    } else {
                        echo "✅ Tests passed!"
                    }
                }
            }
        }

        stage('Upload Reports to S3') {
            steps {
                echo '📤 Uploading reports to S3...'
                script {
                    try {
                        def s3Path = "s3://${S3_BUCKET}/${S3_PREFIX}/build-${BUILD_NUMBER}/${BUILD_TIMESTAMP}"

                        if (isUnix()) {
                            sh """
                                mkdir -p essential-reports

                                cp -r target/surefire-reports/* essential-reports/ || true
                                cp -r target/cucumber-reports/* essential-reports/ || true
                                cp -r target/site/jacoco/* essential-reports/ || true
                                cp -r test-output/SparkReports/* essential-reports/ || true

                                cat > essential-reports/build-metadata.json <<EOF
{
  "build_number": "${BUILD_NUMBER}",
  "status": "${currentBuild.result ?: 'SUCCESS'}",
  "browser": "${params.BROWSER}",
  "environment": "${params.ENV}",
  "timestamp": "${BUILD_TIMESTAMP}",
  "job": "${JOB_NAME}",
  "url": "${BUILD_URL}",
  "git_branch": "${GIT_BRANCH}",
  "git_commit": "${GIT_COMMIT}"
}
EOF

                                aws s3 cp essential-reports ${s3Path}/ --recursive --region ${AWS_REGION}
                            """
                        } else {
                            bat """
                                mkdir essential-reports

                                xcopy target\\surefire-reports essential-reports /E /I /Y
                                xcopy target\\cucumber-reports essential-reports /E /I /Y
                                xcopy target\\site\\jacoco essential-reports /E /I /Y
                                xcopy "test-output\\SparkReports\\*" essential-reports /E /I /Y

                                echo { > essential-reports\\build-metadata.json
                                echo   "build_number": "${BUILD_NUMBER}", >> essential-reports\\build-metadata.json
                                echo   "status": "${currentBuild.result ?: 'SUCCESS'}" >> essential-reports\\build-metadata.json
                                echo } >> essential-reports\\build-metadata.json

                                aws s3 cp essential-reports ${s3Path}/ --recursive --region ${AWS_REGION}
                            """
                        }

                        def url = "https://${S3_BUCKET}.s3.${AWS_REGION}.amazonaws.com/${S3_PREFIX}/build-${BUILD_NUMBER}/${BUILD_TIMESTAMP}"
                        echo "📊 S3 Reports: ${url}"

                    } catch (Exception e) {
                        echo "⚠️ S3 Upload Failed: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo '🚀 Deploying application...'
                // Add deployment steps here
            }
        }
    }

    post {

        always {
            echo '📊 Publishing Reports...'

            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports',
                reportFiles: 'index.html',
                reportName: 'Cucumber Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Surefire Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'test-output/SparkReports',
                reportFiles: 'Report/TestRunReport.html',
                reportName: 'Extent Report'
            ])

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Report'
            ])

            archiveArtifacts artifacts: '**/target/**/*.*, **/test-output/**/*.*', allowEmptyArchive: true

            cleanWs()
        }

        success {
            echo '✅ Build SUCCESS'
        }

        failure {
            echo '❌ Build FAILED'
        }

        unstable {
            echo '⚠️ Build UNSTABLE'
        }
    }
}