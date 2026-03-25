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
        choice(name: 'BROWSER', choices: ['Chrome', 'Firefox'], description: 'Select browser to run tests')
        choice(name: 'ENV', choices: ['QA', 'DEV', 'PROD'], description: 'Select environment')
    }

    environment {
        MAVEN_OPTS    = '-Xmx1024m'
        // S3 bucket where reports are uploaded after every build.
        // Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY as Jenkins credentials (secret text).
        S3_BUCKET     = 'your-s3-bucket-name'          // <-- replace with your bucket name
        S3_REGION     = 'us-east-1'                     // <-- replace with your bucket region
        S3_REPORT_PREFIX = "reports/${env.JOB_NAME}/${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔍 Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Install Playwright') {
            steps {
                echo '🎭 Installing Playwright browsers...'
                bat '''
                    mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
                '''
            }
        }

        stage('Run Tests') {
            steps {
                echo '🧪 Running Playwright tests...'
                echo "Browser: ${params.BROWSER}"
                echo "Environment: ${params.ENV}"
                script {
                    // Run verify (not just test) so JaCoCo report is generated in target/site/jacoco
                    def testResult = bat(script: "mvn clean verify -Dbrowser=${params.BROWSER} -Denv=${params.ENV}", returnStatus: true)
                    
                    // Explicitly fail if tests failed
                    if (testResult != 0) {
                        currentBuild.result = 'FAILURE'
                        error("❌ Tests failed! Stopping pipeline.")
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo '🚀 Deploying application...'
                // Your deployment steps here
            }
        }

        stage('Upload Reports to S3') {
            steps {
                echo '☁️ Uploading test reports to S3...'
                withCredentials([
                    string(credentialsId: 'AWS_ACCESS_KEY_ID',     variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    script {
                        def s3Path = "s3://${S3_BUCKET}/${S3_REPORT_PREFIX}"
                        // Upload Extent Spark Report
                        bat "aws s3 cp test-output/SparkReports/Report/TestRunReport.html ${s3Path}/extent/TestRunReport.html --region ${S3_REGION} || echo Extent report not found, skipping"
                        // Upload screenshots
                        bat "aws s3 cp test-output/SparkReports/Screenshots/ ${s3Path}/extent/Screenshots/ --recursive --region ${S3_REGION} || echo Screenshots not found, skipping"
                        // Upload Cucumber JSON report
                        bat "aws s3 cp target/cucumber-reports/Cucumber.json ${s3Path}/cucumber/Cucumber.json --region ${S3_REGION} || echo Cucumber JSON not found, skipping"
                        // Upload JaCoCo HTML coverage report
                        bat "aws s3 cp target/site/jacoco/ ${s3Path}/jacoco/ --recursive --region ${S3_REGION} || echo JaCoCo report not found, skipping"
                        // Upload Surefire XML results
                        bat "aws s3 cp target/surefire-reports/ ${s3Path}/surefire/ --recursive --region ${S3_REGION} || echo Surefire reports not found, skipping"
                        echo "✅ Reports uploaded to ${s3Path}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo '📊 Publishing test results and reports...'

            // Publish JUnit XML test results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            // Publish Cucumber HTML Report (if it exists)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports',
                reportFiles: 'index.html',
                reportName: 'Cucumber HTML Report',
                reportTitles: 'Test Report'
            ])

            // Publish Surefire HTML Report
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/surefire-reports',
                reportFiles: 'index.html',
                reportName: 'Surefire HTML Report',
                reportTitles: 'Surefire Report'
            ])

            // Publish ExtentReports (Spark Reports)
            // extent.properties outputs to test-output/SparkReports/Report/TestRunReport.html (fixed path, no timestamp)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'test-output/SparkReports',
                reportFiles: 'Report/TestRunReport.html',
                reportName: 'Extent Spark Report',
                reportTitles: 'Extent Test Report'
            ])

            // Publish JaCoCo Coverage Report (generated by mvn verify)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report',
                reportTitles: 'Code Coverage'
            ])

            // Archive all reports as artifacts
            archiveArtifacts artifacts: '**/target/cucumber-reports/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/surefire-reports/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/site/jacoco/**/*.*', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/test-output/SparkReports/**/*.*', allowEmptyArchive: true

            // Clean workspace after build
            cleanWs(cleanWhenNotBuilt: false,
                    deleteDirs: true,
                    disableDeferredWipeout: true,
                    notFailBuild: true,
                    patterns: [[pattern: 'target/**', type: 'INCLUDE']])
        }
        success {
            echo '✅ Tests passed successfully!'
            // Uncomment below to enable email notifications
            // emailext(
            //     subject: "✅ Jenkins Build SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: """
            //         Build Status: SUCCESS
            //         Job: ${env.JOB_NAME}
            //         Build Number: ${env.BUILD_NUMBER}
            //         Browser: ${params.BROWSER}
            //         Environment: ${params.ENV}
            //         
            //         View Reports: ${env.BUILD_URL}
            //     """,
            //     to: 'ndpachkate17801@gmail.com',
            //     attachLog: false
            // )
        }
        failure {
            echo '❌ Tests failed! Pipeline stopped.'
            // Uncomment below to enable email notifications
            // emailext(
            //     subject: "❌ Jenkins Build FAILED - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: """
            //         Build Status: FAILED
            //         Job: ${env.JOB_NAME}
            //         Build Number: ${env.BUILD_NUMBER}
            //         Browser: ${params.BROWSER}
            //         Environment: ${params.ENV}
            //         
            //         Check Console Output: ${env.BUILD_URL}console
            //     """,
            //     to: 'ndpachkate17801@gmail.com',
            //     attachLog: true
            // )
        }
        unstable {
            echo '⚠️ Build is unstable!'
        }
    }
}