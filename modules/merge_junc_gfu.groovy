// MODULE MERGE JUNC FILES

@preserve
merge_junc_gfu =
{
    var rename: false

    doc title: "GFU: merge junc files",
        desc: "Merge junc files",
        constraints: "Should be placed after merge_bam_gfu in order to forward $input.bam to the next stage",
        author: "davide.rambaldi@gmail.com"

    def output_junc = input.prefix.replaceFirst(~/\.[^\.]+$/, '') + ".junc"
    exec"""
        echo -e "[merge_junc_gfu]: Merging junc files in $output_junc" >&2;
        JUNC=\$(ls *.junc 2>/dev/null);
        touch $output_junc;
        for F in $JUNC; do
            if [[ -e $F ]]; then
                cat $F >> $output_junc;
            fi;
        done;
    """
    forward input.bam
}