EXTRA_VARS="dockerize_app_name=$APP_NAME dockerize_app_version=$APP_VERSION" 
EXTRA_VARS="$EXTRA_VARS dockerize_app_conf_file=/opt/app/conf/$DC_ENVIRONMENT.yml"
EXTRA_VARS="$EXTRA_VARS nginx_docker_container_port=5000"

cd $productDslRepo/ansible 

# Install required Ansible roles
ansible-galaxy install -r requirements.yml -f -p galaxy_roles/

ansible-playbook -i environments/$DC_ENVIRONMENT/inventory playbooks/dockerize.yml -e "$EXTRA_VARS"

EXTRA_VARS="kong_api_obj_name=${svc} kong_api_obj_request_path='/${svc}-service'"
EXTRA_VARS="\$EXTRA_VARS kong_api_obj_upstream_url='http://sweb.\$DC_ENVIRONMENT.priority-infra.co.uk/${svc}-service/${svc}'\"\n" +
EXTRA_VARS="\$EXTRA_VARS kong_api_obj_preserve_host=false kong_api_obj_strip_request_path=true\"\n" +
cd $productDslRepo/ansible\n" + 
ansible-playbook -i environments/\$DC_ENVIRONMENT/inventory playbooks/kong_api_obj.yml -e \"\$EXTRA_VARS\""
