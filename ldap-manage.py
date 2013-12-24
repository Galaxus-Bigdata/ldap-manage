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
	ldap-manage.py -bs <domain>
	ldap-manage.py (-h | --help) 
	ldap-manage.py --version 

OPTIONS
	-bs, --build-server 	It will install ldap server and will add rootdn 
				Example :- ldap-manage.py --build-server example.com
				
"""

from docopt import docopt

if __name__ == '__main__':
	args = docopt(__doc__,version='0.1')
	print(args)
