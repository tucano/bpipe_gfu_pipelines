// MODULE CONFIG LUSTRE FS
LSF="/usr/bin/lfs"

@intermediate
set_stripe_gfu =
{
    doc title: "GFU: Set lustre options for working directory",
        desc: """
            Lustre options: 
                -c -1 : a stripe_count of -1 means to stripe over all available OSTs.
                -i -1 : a start_ost_index of -1 allows the MDS to choose the starting index and it is strongly recommended, as this allows space and load balancing to be done by the MDS as needed.
                -s 2M : Stripsize 2 megabytes
        """,
        constraints: "It is a non blocking stage (Fails in non lustre fs, but will return always true).",
        author: "davide.rambaldi@gmail.com"

    produce("setstripe.log") {
        exec """
            $LSF setstripe -c -1 -i -1 -s 2M . 1>/dev/null 2>&1 || true;
            $LSF getstripe . 1> $output 2>&1 || true;
        """
    }
}