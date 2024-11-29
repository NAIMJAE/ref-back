package kr.co.reference.bitCoin;

public class Coin {
    private String symbol;
    private String exchange;
    private String status;

    public Coin(String symbol, String exchange, String status) {
        this.symbol = symbol;
        this.exchange = exchange;
        this.status = status;
    }
    public void getCoin() {
        System.out.println("symbol : " + this.symbol + " | exchange : " + this.exchange + " | status : " + this.status);
    }
    
    public String getSymbol() {
        return symbol;
    }
}
