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
include_recipe 'wildfly::install'