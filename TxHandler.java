// Robert Quintanilla qsy775 
// Eloy Rodriguez fdp418
import java.util.*;
public class TxHandler {

	private UTXOPool utxoPool;
	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS (it starts with a constructor)
		
		// This is our constructor 
		this.utxoPool = new UTXOPool(utxoPool);
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
		HashSet<UTXO> Utxo = new HashSet<UTXO>(); // This will store all the unique UTXO's
		double input = 0.0; // To store input sum of tx
		double output = 0.0; // To store output sum of tx 
		int i;

		// Here we iterate through all the transaction inputs
		for(i = 0; i < tx.numInputs(); i++) 
		{ 
			// Create a tx for each input and get its value to add its hash of its previous value to the UTXO pool 
			Transaction.Input in = tx.getInput(i);

			// Add its hash of its previous value to the UTXO pool
			UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
			
			// (1) If there is an output that is NOT claimed by a transaction then we return false (1)
			if(!this.utxoPool.contains(ut)) 
			{ 
				return false;
			}

			// Set the value of a previous tx to the value of the current tx's value
			double outputValPrev = utxoPool.getTxOutput(ut).value;
			input += outputValPrev;

			// (2) Here we are checking to make sure that all sigs on each input of a tx are valid (2)
			if(!utxoPool.getTxOutput(ut).address.verifySignature(tx.getRawDataToSign(i), in.signature)) 
			{ 
				return false;
			}
			// (3) If there is a UTXO that is claimed multiple times by a tx then we return false (3)
			if(Utxo.contains(ut)) 
			{ 
				return false;
			}
			Utxo.add(ut);
		}
		
		// Iterate through all the tx outputs and retrieve them
		for(Transaction.Output out : tx.getOutputs()) 
		{
			// (4) checking if utxo output values are negative if so, return false (4)
			if(out.value < 0.0) 
			{ 
				return false;
			}
			output += out.value;
		}
		
		// (5) Here we check if the output value is greater than the input value we return false (5)
		if(output > input) 
		{ 
			return false;
		}

		// We return ture if all the previous checks pass
		return true;
		
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

		// We iterate through the number of possibleTxs passed to this method
		for(Transaction tx : possibleTxs) 
		{
			// Here we check to see if a current tx is valid and if it is add it to the hashset pool 
			if(isValidTx(tx)) 
			{
				// Here we add the valid tx to the hashset tx pool 
				validTx.add(tx);
				
				// Iterate through the inputs of a tx that is valid
				for(Transaction.Input in : tx.getInputs()) 
				{
					// Fetch the hash of a previous tx
					UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
					
					// Remove the current utxo from the pool
					utxoPool.removeUTXO(utxo);

				}
				
				// Iterate through a tx's outputs and then retrieve them
				for(i = 0; i < tx.numOutputs(); i++) 
				{ 
					// Create and fetch a tx's ouput 
					Transaction.Output out = tx.getOutput(i);

					// Create a UTXO for each of a tx's outputs
					UTXO utxo = new UTXO(tx.getHash(), i);

					// Add each UTXO to the pool
					utxoPool.addUTXO(utxo, out);
				}
			}
		}
		
		// Create a new Tx array to store the valid processed txs and return them
		Transaction[] validTxArray = new Transaction[validTx.size()];

		// Return an array of valid txs
		return validTx.toArray(validTxArray);
	}

} 
