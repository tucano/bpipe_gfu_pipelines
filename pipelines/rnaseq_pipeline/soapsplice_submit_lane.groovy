// BPIPE PIPELINE to compute all alignment in a dir with soapsplice
about title: "RNA alignment pipeline with soapsplice: IOS GFU 009."

// Usage: bpipe run -r soapsplice_submit_lane.groovy data/*.fastq.gz 

// SOFTWARES PATHS AND OPTIONS
SSPLICE="/lustre1/tools/bin/soapsplice"
SAMTOOLS="/usr/local/cluster/bin/samtools"
SSPLICEOPT_ALN="-f 2 -q 1 -j 0"
PICMERGE="/usr/local/cluster/bin/MergeSamFiles.jar"
VALIDATION_STRINGENCY="SILENT"
CREATE_INDEX=true
MSD=true
ASSUME_SORTED=true

// File Paths
SAMPLESHEET="SampleSheet.csv"
GENOMES_PREFIX="/lustre1/genomes"
SCRATCH_PREFIX="/lustre2/scratch"
ENVIRONMENT_FILE="gfu_environment.sh"

// SPECIFIC SCRIPTS (FIXME now in my home)
GFU_PREPARE_LANE_SCRIPT = "/home/drambaldi/bpipe_gfu_pipelines/bin/prepare_lane.sh"

prepare_lane_gfu_soapsplice = {
	doc title: "Soapsplice RNA alignment preprocessing",
		desc: "Extract from $SAMPLESHEET info about the experiment, create the LOCAL_SCRATCH directory, prepare the gfu_enviroment.sh file",
		constraints: "search for a $SAMPLESHEET file in current working dir",
		author: "davide.rambaldi@gmail.com"
	exec "$GFU_PREPARE_LANE_SCRIPT $SAMPLESHEET $GENOMES_PREFIX $SCRATCH_PREFIX > $ENVIRONMENT_FILE"
}

clean_scratch_gfu_soapsplice = {
	doc title: "Soapsplice RNA alignment postprocessing cleanup",
		desc: "Clean the scratch directory located in $SCRATCH_PREFIX",
		author: "davide.rambaldi@gmail.com"
	exec """
		echo "[clean_scratch_gfu_soapsplice]: Collecting variables from $ENVIRONMENT_FILE" >&2;
		source $ENVIRONMENT_FILE;
		echo "[clean_scratch_gfu_soapsplice]: Removing scratch directory ${LOCAL_SCRATCH}" >&2;
		rm -rf \${LOCAL_SCRATCH}
	"""
}

@Transform("bam")
align_gfu_soapsplice = {
	doc title: "Soapsplice alignment task",
		desc: "Align with soapsplice. Generate temporary files in /dev/shm on the node",
		author: "davide.rambaldi@gmail.com"
	from("fastq.gz","fastq.gz") produce(input.prefix - ".fastq" +".bam") {
		exec"""
			source $ENVIRONMENT_FILE;			
			TMP_SCRATCH=\$(/bin/mktemp -d /dev/shm/\${PROJECTNAME}.XXXXXXXXXXXXX);
			
			LANE=`echo $input1 | rev | cut -d'_' -f 3 | rev`;
			INDEX=`echo $input1 | rev| cut -d'_' -f 1 | rev`;
			INDX=` echo $INDEX | cut -d'.' -f 1 `;

			HEADER_FILE=\${TMP_SCRATCH}/\${PROJECTNAME}_\${RINDEX}_${LANE}_\${INDX}.header;
		
			SSVERSION=`$SSPLICE | head -n1 | awk '{print \$3}'`;

			awk '{OFS="\\t"; print "@SQ","SN:"\$1,"LN:"\$2}' $REFERENCE_FAIDX > $HEADER_FILE;
			echo -e "@RG\\tID:$PROJECTNAME"_"$LANE\\tPL:illumina\\tPU:$FCID\\tLB:$PROJECTNAME\\tSM:$PROJECTNAME\\tCN:CTGB" >> $HEADER_FILE;
			echo -e "@PG\\tID:soapsplice\\tPN:soapsplice\\tVN:$SSVERSION" >> $HEADER_FILE
			
			echo -e "[align_gfu_soapsplice]: Temporary RAM directory on node is $TMP_SCRATCH" >&2;
			echo -e "[align_gfu_soapsplice]: Project is $PROJECTNAME, Rindex is $RINDEX lane is $LANE, indx is $INDX" >&2;
			echo -e "[align_gfu_soapsplice]: Soapsplice version $SSVERSION" >&2;
			echo -e "[align_gfu_soapsplice]: Creating headers using $REFERENCE_FAIDX in file $HEADER_FILE" >&2;

			TMP_OUTPUT=\${TMP_SCRATCH}/$output.prefix

			$SSPLICE -d $REFERENCE_GENOME -1 $input1 -2 $input2 -o \${TMP_OUTPUT} -p 4 $SSPLICEOPT_ALN

			BAM_TMP_OUTPUT=\${TMP_SCRATCH}/$output.prefix

			cat \${HEADER_FILE} \${TMP_OUTPUT} | $SAMTOOLS view -Su - | $SAMTOOLS sort - ${BAM_TMP_OUTPUT}

			mv \${BAM_TMP_OUTPUT}.bam \${LOCAL_SCRATCH}
			mv \${TMP_SCRATCH}*.junc \${LOCAL_SCRATCH}

			echo -e "[align_gfu_soapsplice]: Removing $TMP_SCRATCH RAM directory in node $HOSTNAME" >&2;
			rm -rf $TMP_SCRATCH;
		"""
	}
}

/* "
 * RUNNER 
 */
Bpipe.run { 
	prepare_lane_gfu_soapsplice + "_R*_%.fastq.gz" * [align_gfu_soapsplice] //+ clean_scratch_gfu_soapsplice 
}