package com.radixdlt.client.core.network;

import com.radixdlt.client.core.network.RadixClient.RadixClientStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.ReplaySubject;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Radix Network manages connections to Node Runners for a given Universe.
 */
public final class RadixNetwork {
	private final static Logger logger = LoggerFactory.getLogger(RadixNetwork.class);

	/**
	 * Cached observable for keeping track of Radix Peers
	 */
	private final Observable<RadixPeer> peers;

	/**
	 * Hot observable which updates subscribers of new connection events
	 */
	private final ConnectableObservable<SimpleImmutableEntry<String,RadixClientStatus>> statusUpdates;

	public RadixNetwork(PeerDiscovery peerDiscovery) {
		this.peers = peerDiscovery.findPeers()
			.doOnNext(peer -> logger.info("Added to peer list: " + peer.getLocation()))
			.replay().autoConnect(2);
		this.statusUpdates = peers.map(RadixPeer::getRadixClient)
			.flatMap(
				client -> client.getStatus().map(
					status -> new SimpleImmutableEntry<>(client.getLocation(),status)
				)
			)
			.publish();
		this.statusUpdates.connect();
	}

	public Observable<SimpleImmutableEntry<String,RadixClientStatus>> connectAndGetStatusUpdates() {
		this.peers.subscribe();
		return this.getStatusUpdates();
	}

	/**
	 * Returns a hot observable of the status of peers
	 *
	 * @return a hot Observable of status of peers
	 */
	public Observable<SimpleImmutableEntry<String,RadixClientStatus>> getStatusUpdates() {
		return statusUpdates;
	}

	public Observable<RadixClient> getRadixClients() {
		return peers.map(RadixPeer::getRadixClient);
	}

	public Observable<RadixClient> getRadixClients(Set<Long> shards) {
		return peers.flatMapMaybe(peer -> peer.servesShards(shards)).map(RadixPeer::getRadixClient);
	}

	public Observable<RadixClient> getRadixClients(Long shard) {
		return this.getRadixClients(Collections.singleton(shard));
	}

	/**
	 * Returns a cold observable of the first peer found which supports
	 * a set short shards which intersects with a given set of shards.
	 *
	 * @param shards set of shards to find an intersection with
	 * @return a cold observable of the first matching Radix client
	 */
	public Single<RadixClient> getRadixClient(Set<Long> shards) {
		return this.getRadixClients(shards)
			.flatMapMaybe(client ->
				client.getStatus()
					.filter(status -> !status.equals(RadixClientStatus.FAILURE))
					.map(status -> client)
					.firstOrError()
					.toMaybe()
			)
			.firstOrError()
		;
	}

	/**
	 * Returns a cold observable of the first peer found which supports
	 * a set short shards which intersects with a given shard
	 *
	 * @param shard a shards to find an intersection with
	 * @return a cold observable of the first matching Radix client
	 */
	public Single<RadixClient> getRadixClient(Long shard) {
		return getRadixClient(Collections.singleton(shard));
	}

	/**
	 * Free resources cleanly to the best of our ability
	 */
	public void close() {
		// TODO: fix concurrency
		// TODO: Cleanup objects, etc.
	}
}
