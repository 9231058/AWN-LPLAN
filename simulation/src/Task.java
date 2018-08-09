public class Task {
    private int processTime;
    private int cluster;

    public Task(int processTime, int cluster) {
        this.processTime = processTime;
        this.cluster = cluster;
    }

    public int getCluster() {
        return cluster;
    }

    public int getProcessTime() {
        return processTime;
    }
}
