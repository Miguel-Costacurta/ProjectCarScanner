package obd.ui;

public interface IDataUpdate {
    void onDataUpdate(double rpm, double tps, double tensao, double lambda);
}
