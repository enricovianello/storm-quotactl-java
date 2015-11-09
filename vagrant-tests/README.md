# Vagrant test VM

## Configuration

To configure the vagrant VM edit `config.rb` file. By default if contains only:

	$git_is_local = true

The variable `git_is_local` is used to allow user to use his local cloned or forked repository by mounting it into the VM.
If you want to use the default git repository and branch hard-coded into Vagrantfile, set that variable to `false`:

	$git_is_local = false

The default hard-coded repository and branch are:

	$git_repo_url = "https://github.com/enricovianello/storm-quotactl-java.git"
	$git_repo_branch = "master" 

To use a different remote repository and/or branch, define new values for `git_repo_url` and `git_repo_branch` variables into `config.rb` file:

	$git_repo_url = "https://github.com/italiangrid/storm-quotactl-java.git"
	$git_repo_branch = "develop"
	$git_is_local = false


## Execute

To launch the VM and tests run:

	vagrant up

from this directory.