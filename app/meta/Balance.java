package meta;

import utils.GlobalCons;

public class Balance {
	
	private Double transfer;
	private Double debt;
	private Double credit;
	private Double balance;
	private String excCode;
	
	public Balance() {
		this.transfer = 0d;
		this.debt = 0d;
		this.credit = 0d;
		this.balance = 0d;
		this.excCode = GlobalCons.defaultExcCode;
	}

	public Balance(Double transfer, Double debt, Double credit, Double balance, String excCode) {
		super();
		this.transfer = transfer;
		this.debt = debt;
		this.credit = credit;
		this.balance = balance;
		this.excCode = excCode;
	}

	public Double getTransfer() {
		if (transfer != null)
			return transfer;
		else
			return 0d;
	}

	public void setTransfer(Double transfer) {
		this.transfer = transfer;
	}

	public Double getDebt() {
		if (debt != null)
			return debt;
		else
			return 0d;
	}

	public void setDebt(Double debt) {
		this.debt = debt;
		setBalance();
	}

	public Double getCredit() {
		if (credit != null)
			return credit;
		else
			return 0d;
	}

	public void setCredit(Double credit) {
		this.credit = credit;
		setBalance();
	}

	public Double getBalance() {
		if (balance != null)
			return balance;
		else
			return 0d;
	}

	private void setBalance() {
		double blnc = 0d;

		if (this.debt != null) blnc = this.debt.doubleValue();
		if (this.credit != null) blnc = blnc - this.credit.doubleValue();

		this.balance = blnc;
	}

	public String getExcCode() {
		return excCode;
	}

	public void setExcCode(String excCode) {
		this.excCode = excCode;
	}

}
