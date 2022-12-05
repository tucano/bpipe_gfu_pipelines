// MODULE SPLIT FASTQ FILE
GFU_SPLIT_FASTQ  = "/home/drambaldi/bpipe_gfu_pipelines/bin/split_fastq.sh"

@intermediate
split_fastq_gfu =
{
    var SPLIT_READS_SIZE : 2000000
    var paired : true

    doc title: "GFU: split fastq.gz files in $SPLIT_READS_SIZE reads/file",
        desc: "Use split to subdivide a fastq pair (R1 and R2) in chunks (paired: true) or a single file (paired: false)",
        constraints: "Work with fastq.gz and fastq files",
        author: "davide.rambaldi@gmail.com"

    def n_lines = SPLIT_READS_SIZE * 4

    produce("*.fastq") {
        if (input.endsWith(".gz")) {
            if (paired) {
                exec"""
                    echo -e "[split_fastq_gfu]: splitting fastq.gz pair $input1 and $input2 on node $HOSTNAME in $n_lines ($SPLIT_READS_SIZE reads) per file" >&2;
                    zcat $input1 | split -l $n_lines -d -a 4 - read1_;
                    for file in read1_*; do mv "$file" "${file}.fastq"; done;
                    zcat $input2 | split -l $n_lines -d -a 4 - read2_;
                    for file in read2_*; do mv "$file" "${file}.fastq"; done;
                """
            } else {
                exec"""
                    echo -e "[split_fastq_gfu]: splitting fastq.gz file on node $HOSTNAME in $n_lines ($SPLIT_READS_SIZE reads) per file" >&2;
                    zcat $input1 | split -l $n_lines -d -a 4 - read_;
                    for file in read_*; do mv "$file" "${file}.fastq"; done;
                """
            }
        } else {
            if (paired) {
                exec"""
                    echo -e "[split_fastq_gfu]: splitting fastq pair on node $HOSTNAME in $n_lines ($SPLIT_READS_SIZE reads) per file" >&2;
                    split -l $n_lines -d -a 4 $input1 read1_;
                    split -l $n_lines -d -a 4 $input2 read2_;
                    for file in read1_*; do mv "$file" "${file}.fastq"; done;
                    for file in read2_*; do mv "$file" "${file}.fastq"; done;
                """
            } else {
                exec"""
                    echo -e "[split_fastq_gfu]: splitting fastq pair on node $HOSTNAME in $n_lines ($SPLIT_READS_SIZE reads) per file" >&2;
                    split -l $n_lines -d -a 4 $input1 read_;
                    for file in read_*; do mv "$file" "${file}.fastq"; done;
                """
            }
        }
    }
}