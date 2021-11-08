import java.security.CryptoPrimitive;
import java.util.*;

public class TxHandler { 
	private UTXOPool utxoPool; // 

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS (it starts with a constructor) 
		this.utxoPool = new UTXOPool();
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS
		UTXOPool Utxos = new UTXOPool(); // This will store all the unique UTXO's
		double OutputSumPrev = 0;
		double OutputSumCurr = 0;
		int i;

		for(i = 0; i < tx.numInputs(); i++) 
		{ 
			Transaction.Input in = tx.getInput(i);
			UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
			Transaction.Output output = utxoPool.getTxOutput(utxo);

			if(!utxoPool.contains(utxo)) 
			{ 
				return false;
			} 
			if(!tx.verifySignature(output.address, tx.getRawDataToSign(i), in.signature)) 
			{ 
				return false; 
			}
			if(Utxos.contains(utxo)) 
			{ 
				return false;
			}
			Utxos.addUTXO(utxo, output);
			OutputSumPrev += output.value;

		} 
		for(Transaction.Output out : tx.getOutputs()) 
		{
			// checking if utxo output values are negative if so, return false 
			if(out.value < 0) 
			{ 
				return false;
			}
			OutputSumCurr += out.value;
		} 
		if(OutputSumPrev >= OutputSumCurr) 
		{ 
			return true;
		} 
		return false;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS

		// This will store/hold valid unprocessed transactions
		HashSet<Transaction> validTx = new HashSet<>();
		int i;

		for(Transaction tx : possibleTxs) 
		{ 
			if(isValidTx(tx)) 
			{ 
				validTx.add(tx); 
				for(Transaction.Input in : tx.getInputs()) 
				{ 
					UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
					utxoPool.removeUTXO(utxo);
				} 
				for(i = 0; i < tx.numOutputs(); i++) 
				{ 
					Transaction.Output out = tx.getOutput(i);
					UTXO utxo = new UTXO(tx.getHash(), i);
					utxoPool.addUTXO(utxo, out);
				}
			}
		} 
		Transaction[] validTxArray = new Transaction[validTx.size()];
		return validTx.toArray(validTxArray);
	}

} 
