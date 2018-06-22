package com.radixdlt.client.core.ledger;

import com.radixdlt.client.assets.Asset;
import com.radixdlt.client.core.atoms.*;
import com.radixdlt.client.core.crypto.ECPublicKey;
import com.radixdlt.client.core.crypto.ECSignature;

import java.util.List;
import java.util.stream.Collectors;

public class RadixAtomValidator implements AtomValidator {
	private final static RadixAtomValidator validator = new RadixAtomValidator();

	public static RadixAtomValidator getInstance() {
		return validator;
	}

	private RadixAtomValidator() {
	}

	/**
	 * Checks the owners of each AbstractConsumable particle and makes sure the
	 * atom contains signatures for each owner
	 *
	 * @param atom atom to validate
	 * @throws AtomValidationException if atom has missing/bad signatures for a particle
	 */
	public void validateSignatures(Atom atom) throws AtomValidationException {
		RadixHash hash = atom.getHash();

		final List<AbstractConsumable> consumables = atom.getParticles().stream()
				.filter(Particle::isAbstractConsumable)
				.map(Particle::getAsAbstractConsumable)
				.filter(particle -> Asset.POW.getId().equals(particle.getAssetId()))
				.collect(Collectors.toList());

		for(AbstractConsumable particle : consumables) {
			if (particle.getOwners().isEmpty()) {
				throw new AtomValidationException("No owners in particle");
			}

			if (particle instanceof Consumer) {
				for (ECPublicKey owner : particle.getOwners()) {
					ECSignature signature = atom.getSignature(owner.getUID())
							.orElseThrow(() -> new ArithmeticException("Missing signature"));

					if (!hash.verifySelf(owner, signature)) {
						throw new AtomValidationException("Bad signature");
					}
				}
			}
		}
	}

	public void validate(Atom atom) throws AtomValidationException{
		// TODO: check with universe genesis timestamp
		if (atom.getTimestamp() == null || atom.getTimestamp() == 0L) {
			throw new AtomValidationException("Null or Zero Timestamp");
		}

		validateSignatures(atom);
	}
}
