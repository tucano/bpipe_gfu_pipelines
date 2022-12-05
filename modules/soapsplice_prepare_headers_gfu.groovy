// MODULE SOAPSPLICE PREPARE HEADERS
SSPLICE="/lustre1/tools/bin/soapsplice"

@intermediate
soapsplice_prepare_headers_gfu = 
{
    // INFO
    doc title: "GFU: prepare header file for alignment with soapsplice",
        desc: "Generate a header file for alignment with soapsplice",
        constraints: "...",
        author: "davide.rambaldi@gmail.com"

    def header  = '@RG' + "\tID:${EXPERIMENT_NAME}\tPL:${PLATFORM}\tPU:${FCID}\tLB:${EXPERIMENT_NAME}\tSM:${SAMPLEID}\tCN:${CENTER}"

    transform("header") {
        exec"""
            SSVERSION=\$($SSPLICE | head -n1 | awk '{print \$3}');
            echo -e "[soapsplice_prepare_headers_gfu] soapsplice version $SSVERSION. Input is: $input";
            awk '{OFS="\t";  print "@SQ","SN:"\$1,"LN:"\$2}' $REFERENCE_FAIDX > $output;
            echo -e "$header" >> $output;
            echo -e "@PG\tID:soapsplice\tPN:soapsplice\tVN:$SSVERSION" >> $output
        """
    }
}