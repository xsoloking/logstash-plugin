node('linux') {  
    stage('pre-commit') {
            sh '''
               which git
               curl https://pre-commit.com/install-local.py | python -
               /home/jenkins/bin/pre-commit
           '''
    }
}
buildPlugin()
