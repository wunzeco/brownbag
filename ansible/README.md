INFRA
====

## Steps

###Install galaxy roles

```
$ ansible-galaxy install -r requirements.yml -f -p galaxy_roles/
```


###Provision & Configure VMs

```
vagrant up
```


###Reconfigure VM

```
vagrant provision
```
