PBSENV_SCRIPT="/home/drambaldi/bpipe_gfu_pipelines/bin/pbs_env.sh"

pbs_env = {
	doc title: "PBS Professional engine environment",
		desc: "Print some PBS engine enviroment variables",
		author: "davide.rambaldi@hsr.it"
	exec "$PBSENV_SCRIPT > $output.txt"
}

Bpipe.run { pbs_env }