package uk.betacraft.auth.jsons.microsoft;

import java.util.Date;

import uk.betacraft.auth.Response;

public class MSErrorResponse extends Response {

	public String error;
	public String error_description;
	public int[] error_codes;
	public Date timestamp;
	public String trace_id;
	public String correlation_id;
}
