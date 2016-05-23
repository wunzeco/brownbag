/*
    Note: To access Jenkins env variables (such as WORKSPACE, BUILD_NUMBER)
           from within DSL scripts just wrap them in '${}'.
*/
if ( !Object.metaClass.hasProperty('BUILD_NUMBER') ) {
    BUILD_NUMBER = '1.0'
} 

print "Build number is => ${BUILD_NUMBER}"

/*
    service/component build job
*/
def svc = 'data_loader'
def rakeScriptsRepo = 'infra-rake-tf-build'
def productDslRepo = 'infra-priority-stack'
job("${svc}-service-build") {
    scm {
        git {
            remote {
                github("wunzeco/brownbag")
            }
        }
    }
    triggers {
        scm('H/2 * * * *')
    }
    steps {
        shell ( "echo 'COMPILATION of code happens here'" )
        shell(
            """#!/bin/bash
            source ~/development/bin/activate
            cd \$WORKSPACE/app
            pip install -r requirements.txt
            echo -e "\\n\\n****** STATIC CODE ANALYSIS" && pylint *.py
            echo -e "\\n\\n****** UNIT TEST" && nosetests -v
            """.stripIndent()
             )
        shell(
            """
            cd \$WORKSPACE/app
            echo -e '****** PACKAGING'
            tar -zcvf data_loader.tar.gz *
            """.stripIndent()
             )
    }
    wrappers {
        colorizeOutput()
    }
    publishers {
        archiveJunit '**/*.tar.gz'
        archiveArtifacts {
            pattern("**/Dockerfile")
            pattern("**/*.tar.gz")
            onlyIfSuccessful()
        }
        downstreamParameterized {
            trigger("${svc}-service-deploy") {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                    predefinedProp('DC_ENVIRONMENT', 'dev')
                    predefinedProp('APP_NAME', "${svc}-service")
                }
            }
        }
    }
}

/*
    svc deploy job
*/
job("${svc}-service-deploy") {
    parameters {
        choiceParam('DC_ENVIRONMENT', ['dev', 'test', 'stage', 'prod'],
                    'Product environment to build')
        stringParam('APP_NAME',     defaultValue = 'soa-cache-service', 
                    description = 'App Name')
    }         
    multiscm {
        git {
            remote {
                github("o2-priority/${productDslRepo}", 'ssh')
                credentials("priority-ci-user-git-creds-id")
            }
            branch('master')
            relativeTargetDir("${productDslRepo}")
        }
    }
    steps {
        shell(
                "EXTRA_VARS=\"dockerize_app_name=\$APP_NAME dockerize_app_version=\$APP_VERSION\"\n" + 
                "EXTRA_VARS=\"\$EXTRA_VARS dockerize_app_conf_file=/opt/app/conf/\$DC_ENVIRONMENT.yml\"\n" +
                "EXTRA_VARS=\"\$EXTRA_VARS nginx_docker_container_port=5000\"\n" +
                "cd $productDslRepo/ansible\n" + 
                "ansible-galaxy install -r requirements.yml -f -p galaxy_roles/\n" +
                "ansible-playbook -i environments/\$DC_ENVIRONMENT/inventory playbooks/dockerize.yml -e \"\$EXTRA_VARS\""
                )
        shell(
                "EXTRA_VARS=\"kong_api_obj_name=${svc} kong_api_obj_request_path='/${svc}-service'\"\n" +
                "EXTRA_VARS=\"\$EXTRA_VARS kong_api_obj_upstream_url='http://sweb.\$DC_ENVIRONMENT.priority-infra.co.uk/${svc}-service/soacache'\"\n" +
                "EXTRA_VARS=\"\$EXTRA_VARS kong_api_obj_preserve_host=false kong_api_obj_strip_request_path=true\"\n" +
                "cd $productDslRepo/ansible\n" + 
                "ansible-playbook -i environments/\$DC_ENVIRONMENT/inventory playbooks/kong_api_obj.yml -e \"\$EXTRA_VARS\""
                )
    }
    wrappers {
        colorizeOutput()
    }
}

/*
    svc functional test job
*/
job("${svc}-service-test") {
    scm {
        git {
            remote {
                github("o2-priority/service-${svc}", "ssh")
                credentials("priority-ci-user-git-creds-id")
            }
            branch('ci-pipeline')
        }
    }
    triggers {
        upstream("${svc}-service-deploy")
    }
    steps {
        shell(
            """
            export DL_HOST=172.20.10.20
            cd \$WORKSPACE/app
            bundle install && cucumber
            """.stripIndent()
             )
    }
}
