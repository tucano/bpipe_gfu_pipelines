// MODULE MERGE BAM FILE
PICMERGE="/usr/local/cluster/bin/MergeSamFiles.jar"

@preserve
merge_bam_gfu = 
{
	doc title: "GFU merge bam files with $PICMERGE",
		desc: "Merge bam files with $PICMERGE, combine junction usage",
		author: "davide.rambaldi@gmail.com"
	def output_prefix = input.prefix.replaceFirst(/_R.*/,"")
	def output_bam = output_prefix + ".merge.bam"
	def output_junc = output_prefix + ".junc"
	input_strings = inputs.collect() { return "I=" + it}.join(" ")
	produce(output_bam, output_junc) 
	{
		exec """
			source $ENVIRONMENT_FILE;
			echo -e "[merge_bam_gfu]: Merging BAM files $inputs in output file $output_bam" >&2;
			java -jar $PICMERGE $input_strings O=$output_bam
				VALIDATION_STRINGENCY=SILENT
				CREATE_INDEX=true
				MSD=true
				ASSUME_SORTED=true
				USE_THREADING=true

			echo -e "[merge_bam_gfu]: Merging junc files in $output_junc" >&2;
			touch $output_junc;
			for F in ${LOCAL_SCRATCH}/*.junc; do
				if [[ -e $F ]]; then
					cat $F >> $output_junc;
				fi;
			done;
		""","merge_bam_files"
	}
}
