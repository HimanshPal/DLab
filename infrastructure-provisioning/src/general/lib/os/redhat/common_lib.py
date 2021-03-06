#!/usr/bin/python

# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ******************************************************************************

from fabric.api import *
from fabric.contrib.files import exists


def ensure_pkg(user, requisites='git vim gcc python-devel openssl-devel nmap libffi libffi-devel'):
    try:
        if not exists('/home/{}/.ensure_dir/pkg_upgraded'.format(user)):
            print("Updating repositories and installing requested tools: {}".format(requisites))
            if sudo("systemctl list-units  --all | grep firewalld | awk '{print $1}'") != '':
                sudo('systemctl disable firewalld.service')
                sudo('systemctl stop firewalld.service')
            sudo('setenforce 0')
            sudo("sed -i '/^SELINUX=/s/SELINUX=.*/SELINUX=disabled/g' /etc/selinux/config")
            sudo('yum update-minimal --security -y')
            sudo('yum -y install wget')
            sudo('wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm')
            sudo('rpm -ivh epel-release-latest-7.noarch.rpm')
            sudo('yum repolist')
            sudo('yum -y install python-pip gcc')
            sudo('rm -f epel-release-latest-7.noarch.rpm')
            sudo('export LC_ALL=C')
            sudo('yum -y install ' + requisites)
            sudo('touch /home/{}/.ensure_dir/pkg_upgraded'.format(user))
        return True
    except:
        return False


def change_pkg_repos():
    if not exists('/tmp/pkg_china_ensured'):
        put('/root/files/sources.list', '/tmp/sources.list')
        sudo('mv /tmp/sources.list  /etc/yum.repos.d/CentOS-Base-aliyun.repo')
        sudo('touch /tmp/pkg_china_ensured')
