node('linux') {  
    stage('pre-commit') {
            sh '''
               curl https://pre-commit.com/install-local.py | python -
               /home/jenkins/bin/pre-commit
           '''
    }
}
buildPlugin()
