print "set admin-host..."
AdminConfig.create('HostAlias', AdminConfig.getid('/Cell:DefaultCell01/VirtualHost:admin_host/'), '[[hostname "*"] [port "7060"]]')
AdminConfig.create('HostAlias', AdminConfig.getid('/Cell:DefaultCell01/VirtualHost:admin_host/'), '[[hostname "*"] [port "7043"]]')
#print "delete *:80..."
#AdminConfig.remove('(cells/DefaultCell01|virtualhosts.xml#HostAlias_2)')
print "set default-host..."
AdminConfig.create('HostAlias', AdminConfig.getid('/Cell:DefaultCell01/VirtualHost:default_host/'), '[[hostname "*"] [port "7080"]]')
AdminConfig.create('HostAlias', AdminConfig.getid('/Cell:DefaultCell01/VirtualHost:default_host/'), '[[hostname "*"] [port "7443"]]')
print "save..."
AdminConfig.save()
