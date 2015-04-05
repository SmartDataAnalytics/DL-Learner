package org.dllearner.utilities.mpi;

public class MPIConfig {
    private int rank;
    // FIXME: only in case of SMPDEV the second argument is nprocesses!!
    private int nprocesses;

    /**
     * native --> native MPI code wrapper
     * niodev --> MPI implemented in Java using java.nio sockets
     * mxdev  --> MPI implemented in Java using Myrinet eXpress (MX) library
     *            for Myrinet networks
     * smpdev --> MPI implemented in Java using shared memory
     * hybdev --> hybrid approach of MPI implemented in Java using
     *            multicore and cluster configuration
     *
     * See http://mpj-express.org/software/mpj-design-newest.png for a design
     * overview.
     */
    public static enum DeviceName {
        NATIVE("native"),
        NIODEV("niodev"),
        MXDEV("mxdev"),
        SMPDEV("smpdev"),
        HYBDEV("hybdev");

        public String strVal;

        DeviceName(String strVal) {
            this.strVal = strVal;
        }
    }
    /** either "native" or */
    private DeviceName deviceName;

    // FIXME: only in case of SMPDEV the second argument is nprocesses!!
    public MPIConfig(int rank, int nprocesses, DeviceName deviceName ) {
        this.rank = rank;
        this.nprocesses = nprocesses;
        this.deviceName = deviceName;
    }

    public String[] asArgv() {
        // FIXME: this only works in case of SMPDEV
        String[] argv = new String[3];

        argv[0] = Integer.toString(rank);
        argv[1] = Integer.toString(nprocesses);  // FIXME: works only with smpdev
        argv[2] = deviceName.strVal;

        return argv;
    }
}
