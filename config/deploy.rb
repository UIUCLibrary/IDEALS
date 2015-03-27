### Capistrano deploy for ideals-dspace
###
### Bill Ingram <wingram2@illinois.edu>
### Tue 09 Jul 2013 10:24:45 AM PDT

require 'open-uri'

set :application, "ideals-dspace"

set :scm, :git
set :repository, "ssh://git@bitbucket.org/UIUCLibrary/ideals-dspace-master.git"

set :production_server, "vanaheim.cites.illinois.edu"
set :staging_server, "vanaheim-dev.cites.illinois.edu"
set :dev_server, "medusa-dev.library.illinois.edu"

default_run_options[:shell] = '/bin/bash -l'

task :production do
  role :web, production_server
  role :app, production_server
  role :db, production_server, :primary => true
end

task :staging do
  role :web, staging_server
  role :app, staging_server
  role :db, staging_server, :primary => true
end

task :dev do
  role :web, dev_server
  role :app, dev_server
  role :db, dev_server, :primary => true
  set :keep_releases, 2
end

set :deploy_via, :copy
set :copy_strategy, :export

set :user, "ideals-dspace"
set :group, "ideals-dspace"

set :service_root, "$HOME"
set :service_email, "ideals-admin@illinois.edu"

set :deploy_to, "#{service_root}/src/#{application}"
set :shared_path, "#{service_root}/src/#{application}/shared"

# set up shared config
set :shared_config, "#{service_root}/src/#{application}/shared/config"

depend :remote, :directory, :service_root, :shared_config

set :use_sudo, false

# Java
set :java_root, "#{service_root}/java"
set :java_home, "#{java_root}/jdk"
set :jre_home, "#{java_home}/jre"
set :jsse_home, "#{jre_home}/lib"
set :java_opts, "-Dhttps.protocols=SSLv3"

# Maven
set :maven_home, "#{service_root}/maven"
set :maven_opts, "-Xms256M -Xmx512M -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/services/scratch"

# Ant
set :ant_home, "#{service_root}/ant"
set :ant_opts, "-Doverwrite=true clean_backups update"

# Tomcat
set :catalina_home, "#{service_root}/tomcat"
set :catalina_opts, "-server -Xms512M -Xmx1024M -Dfile.encoding=UTF-8"
set :tomcat_home, "#{catalina_home}"

# Postgres
set :pg_home, "#{service_root}/pgsql"
set :pg_data, "#{pg_home}/databases"
set :pg_host, "#{pg_home}/run"

# DSpace
set :dspace_name, "dspace"
set :dspace_version, "3.2"
set :dspace_home, "#{service_root}/dspace"
set :dspace_source, "#{deploy_to}"
set :dspace_db_user, "dspace"
set :dspace_db_name, "dspace"

# ClamAV
set :clamav_home, "#{service_root}/clamav"


namespace :tomcat do

  desc "Start Tomcat"
  task :start, :roles => [:app] do
    run "#{service_root}/bin/start-tomcat"
  end

  desc "Stop Tomcat"
  task :stop, :roles => [:app] do
    begin
      run "#{service_root}/bin/stop-tomcat"
    rescue RuntimeError => e
      # skip it
    end
  end

  desc "Clean Tomcat cache"
  task :clean, :roles => [:app] do
    run "#{service_root}/bin/clean-tomcat"
  end

  desc "Restart Tomcat"
  task :restart, :roles => [:app] do
    run "#{service_root}/bin/restart-tomcat"
  end

  desc "Tail tomcat/logs/catalina.out"
  task :tail, :roles => [:app] do
    stream "tail -f #{tomcat_home}/logs/catalina.out"
  end

end

namespace :pg do

  desc "Start PostgreSQL"
  task :start, :roles => [:db] do
    run "#{service_root}/bin/start-postgres"
  end

  desc "Stop PostgreSQL"
  task :stop, :roles => [:db] do
    begin
      run "#{service_root}/bin/stop-postgres"
    rescue RuntimeError => e
      # skip it
    end
  end

  desc 'Backup DB'
  task :backup_dspace_db, :roles => :db do
    stamp = Time.now.utc.strftime("%Y%m%d%H%M.%S")
    run "cd #{service_root} && PGDATA=#{pg_data} PGHOST=#{pg_host} pg_dump -E UNICODE -f tmp/dspace-data.#{stamp} #{dspace_db_name}"
    run "cd #{service_root}/tmp && gzip -9 dspace-data.#{stamp}"
  end

end

namespace :dspace do

  desc "Build dspace with maven"
  task :build, :roles => [:app] do
    run "cd #{deploy_to}/current && JAVA_HOME=#{java_home} MAVEN_OPTS=\"#{maven_opts}\" #{maven_home}/bin/mvn  clean package"
  end

  desc "Deploy DSpace with Ant"
  task :deploy, :roles => [:app] do
    run "cd #{deploy_to}/current/#{dspace_name}/target/#{dspace_name}-#{dspace_version}-build && JAVA_HOME=#{java_home} #{ant_home}/bin/ant fresh-install"
  end

  desc "Update DSpace with Ant"
  task :update, :roles => [:app] do
    run "cd #{deploy_to}/current/#{dspace_name}/target/#{dspace_name}-#{dspace_version}-build && JAVA_HOME=#{java_home} #{ant_home}/bin/ant #{ant_opts}"
  end

end


namespace :deploy do

  desc "Makes the latest release group writeable"
  task :finalize_update, :except => {:no_release => true} do
    run "chmod -R g+w #{latest_release}" if fetch(:group_writable, true)
  end

  desc "Restart Postgres and Tomcat"
  task :restart, :roles => [:app, :db] do
    pg.stop
    tomcat.stop
    tomcat.clean
    pg.start
    tomcat.start
  end

  desc "create a config directory under shared"
  task :create_shared_config do
    run "mkdir #{shared_path}/config"
  end

  desc "link shared configuration"
  task :link_config do
    ['build.properties'].each do |file|
      run "ln -nfs #{shared_config}/#{file} #{current_path}/#{file}"
    end
    ['xhtml-head-item.properties'].each do |file|
      run "ln -nfs #{shared_config}/#{file} #{current_path}/dspace/config/crosswalks/#{file}"
    end
  end

  desc "Remove old DSpace backup installations. As is (because of the path) this only works on dev."
  task :remove_dspace_backups do
    ['bin', 'etc', 'lib', 'webapps'].each do |prefix|
      run "rm -rf /home/ideals-dspace/dspace/#{prefix}.bak-*"
    end
  end

end

# Some before and after hooks for regular deploy
after 'deploy:setup', 'deploy:create_shared_config'
after 'deploy:create_symlink', 'deploy:link_config'
after 'deploy:update', 'dspace:build', 'dspace:update'


###
### Unused tasks
###

#
# Disable all the default tasks that
# either don't apply, or I haven't made work.
#
namespace :deploy do
  [:upload, :start, :stop, :migrate, :migrations].each do |default_task|
    desc "[internal] disabled"
    task default_task do
      # disabled
    end
  end

  namespace :web do
    [:disable, :enable].each do |default_task|
      desc "[internal] disabled"
      task default_task do
        # disabled
      end
    end
  end

  namespace :pending do
    [:default, :diff].each do |default_task|
      desc "[internal] disabled"
      task default_task do
        # disabled
      end
    end
  end
end
