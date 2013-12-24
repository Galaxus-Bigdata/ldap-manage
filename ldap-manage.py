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
	ldap-manage.py --build-server <domain>
	ldap-manage.py (-h | --help) 
	ldap-manage.py --version 

OPTIONS
	 --build-server 	It will install ldap server and will add rootdn 
				Example :- ldap-manage.py --build-server example.com
				
"""
from __future__ import print_function
from docopt import docopt
import subprocess
import os
import sys
import apt

def build_ldap_server(domain):
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
				print("Sorry, package installed failed [ {err}]".format(err=str(arg)),
					file=sys.stderr)


if __name__ == '__main__':
	args = docopt(__doc__,version='0.1')
	if args['--build-server']:
		build_ldap_server(args['<domain>'])
	else:
		print("not install")
	print(args)
