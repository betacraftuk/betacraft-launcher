package uk.betacraft.auth.jsons.microsoft;

public class XBLXSTSAuthResponse extends XSTSErrorResponse {

	public String IssueInstant;
	public String NotAfter;
	public String Token;
	public XBLDisplayClaims DisplayClaims;

	public boolean isEmpty() {
		return this.Token == null && this.NotAfter == null && this.IssueInstant == null;
	}
}
