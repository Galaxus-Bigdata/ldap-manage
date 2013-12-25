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
import ldap
import platform
import shutil
import subprocess

if "centos" in platform.dist():
	centos = True
else:	centos = False

def install():
	if centos:
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
	else:
		import apt
		packages = [ 'slapd', 'ldap-utils' ]
		cache = apt.cache.Cache()	
		cache.update()
		for pkg in packages:
			pkg = cache[pkg]
			if pkg.is_installed:
				print("{0} already installed".format(pkg))
			else:
				pkg.mark_install()
				try:
					cache.commit()
				except	Exception,arg:
					print("Sorry, package installed failed [ {err}]".format
						(err=str(arg)),file=sys.stderr)

def configure_ldap(domain,password):
	try:
		slapd_sample = "/usr/share/openldap-servers/slapd.conf.obsolete"
		DB_sample = "/usr/share/openldap-servers/DB_CONFIG.example"
		DB_config = "/var/lib/ldap/DB_CONFIG"
		slapd_conf = "/etc/openldap/slapd.conf"
		shutil.copy2(slapd_sample,slapd_conf)
		shutil.copy2(DB_sample,DB_config)
		for path,dir,files in os.walk("/var/lib/ldap/"):
			for file in files:
				f = ''.join([ path, file ])
				uid = int(subprocess.Popen([ 'id', '-u', 'ldap'],
					stdout=subprocess.PIPE).communicate()[0])
				os.chown(f,uid,uid)	
	except	Exception,arg:
		print("Someing issue with coping slapd.conf file",arg)
	
	crypt = subprocess.Popen(['slappasswd','-s',password],
		subprocess.PIPE).communicate()[0]
	return crypt


if __name__ == '__main__':
	args = docopt(__doc__,version='0.1')
	if args['--build-server']:
		install()
		configure_ldap(args['<domain>'],args['<password>'])
