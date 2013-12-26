#!/usr/bin/env python

"""
NAME
	ldap-manage.py - manage ldap server

SYNOPSIS
	ldap-manage.py	[ OPTIONS ]	

DESCRIPTION
	ldap-manage.py script will manage following things :
		- Build Ldap Server 
		- Remove Ldap Server
		- Add/Remove Users and Groups
		- Add/Remove ACL's
		- Backup Ldap Server
		- Restore Ldap Backup
Usage:
	ldap-manage.py --build-server <domain> --password <password>
	ldap-manage.py (-h | --help) 
	ldap-manage.py --version 

OPTIONS
	 --build-server 	It will install ldap server and will add rootdn 
				Example :- ldap-manage.py --build-server example.com

	 --password		specify password for ldap Manager/admin user
				
"""
from __future__ import print_function
from docopt import docopt
import os
import sys
import pwd
import ldap
import shutil
import platform
import urllib2
import subprocess

if "centos" in platform.dist()[0].lower():
	ostype = "centos"
	DB_sample = "/usr/share/openldap-servers/DB_CONFIG.example"
	DB_config = "/var/lib/ldap/DB_CONFIG"
	slapd_conf = "/etc/openldap/slapd.conf"
	uid = pwd.getpwnam("ldap").pw_uid
	gid = pwd.getpwnam("ldap").pw_gid
elif "ubuntu" in platform.dist()[0].lower():
	uid = pwd.getpwnam("openldap").pw_uid
	gid = pwd.getpwnam("openldap").pw_gid
	ostype = "ubuntu"
	DB_sample= "/usr/share/doc/slapd/examples/DB_CONFIG"
	DB_config = "/var/lib/ldap/DB_CONFIG"
	slapd_conf = "/etc/ldap/slapd.conf"
else:	
	ostype = "unknow"

def install():
	if ostype == "centos":
		import yum
		yb = yum.YumBase()
		packages = [ 'openldap-clients','openldap-servers' ]
		for pkg in packages:
			if yb.rpmdb.searchNevra(name=pkg):
				print("{0} package already installed".format(pkg))
				install = False
			else:
				install = True
				print("Installing {0}".format(pkg))
				yb.install(name=pkg)
				yb.resolveDeps()
		if install:
			yb.buildTransaction()
			yb.processTransaction()
	elif ostype == "ubuntu":
		import apt
		packages = [ 'slapd', 'ldap-utils' ]
		cache = apt.cache.Cache()	
		cache.update()
		for pkg in packages:
			p = cache[pkg]
			if p.is_installed:
				print("{0} already installed".format(pkg))
			else:
				pkg.mark_install()
				try:
					cache.commit()
				except	Exception,arg:
					print("Sorry, package installed failed [ {err}]".format
						(err=str(arg)),file=sys.stderr)
	else: print("Sorry, OStype :{0}".format(ostype),file=sys.stderr)

def configure_ldap(domain,password):
	dc1,dc2 = domain.split('.')
	link='https://raw.github.com/rahulinux/ldap-manage/master/slapd.conf.sample'
	try:
		slapd_sample = "/tmp/slapd.conf.sample"
		open(slapd_sample,"wb").write(
			urllib2.urlopen(link).read())
		shutil.copy2(slapd_sample,slapd_conf)
		shutil.copy2(DB_sample,DB_config)
		for path,dir,files in os.walk("/var/lib/ldap/"):
			for file in files:
				f = ''.join([ path, file ])
				os.chown(f,uid,gid)	
	except	Exception,arg:
		print("Someing issue with coping slapd.conf file",arg)
		
	crypt = subprocess.Popen(['slappasswd','-s',password],
		subprocess.PIPE).communicate()[0]
	crypt = str(crypt)
	print(type(crypt))
	f = open(slapd_sample,'r')
	slapd = open(slapd_conf,'wb')
	for line in f:
		l = line
		if 'my-domain'in l: 
			l = l.replace('my-domain',dc1)
			l = l.replace('com',dc2)
		if 'rootpw' in l:
			l = l.replace('secret',crypt)
		slapd.write(l)
	f.close()
	slapd.close()
	


if __name__ == '__main__':
	args = docopt(__doc__,version='0.1')
	if args['--build-server']:
		domain,password = args['<domain>'],args['<password>']
		if len(domain.split('.')) != 2:
			print("Incorrect Domain name")
			sys.exit(1)
		#install()
		configure_ldap(domain,password)
