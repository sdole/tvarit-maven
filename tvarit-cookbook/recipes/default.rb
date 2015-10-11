#
# Cookbook Name:: tvarit-cookbook
# Recipe:: default
#
# Copyright (C) 2015 YOUR_NAME
#
# All rights reserved - Do Not Redistribute
#
Chef::Log.debug('aha!')
Chef::Log.fatal('aha fatal!')
include_recipe 'java'
Chef::Log.debug('after include java!')
Chef::Log.fatal('after include java!')
#include_recipe 'wildfly::install'
Chef::Log.debug('after include java!!')
Chef::Log.fatal('after include wildfly')

