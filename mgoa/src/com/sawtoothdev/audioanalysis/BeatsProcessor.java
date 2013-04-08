package com.sawtoothdev.audioanalysis;

import java.util.ArrayList;
import java.util.Iterator;


	/**
	* Post processor for use with Beat collections returned by FastBeatDetector.
 	* 
 	* @author albatross
 	*/
	public class BeatsProcessor {


		/***
		 * Processes a collection of beats to ensure a minimum amount of time is present
		 * between consecutive beats and returns the result. If two consecutive beats are too
		 * close together, then the one with the higher energy will be kept.
		 * 
		 * Does not modify the collection passed to it.
		 * @param beats The collection to process
		 * @param minTimeBetween The minimum time, in milliseconds, that must be present
		 * between consecutive beats.
		 */
		public static ArrayList<Beat> removeCloseBeats(ArrayList<Beat> beats, long minTimeBetween) {

			ArrayList<Beat> result = new ArrayList<Beat>();

			Iterator<Beat> iterator = beats.iterator();

			Beat prevBeat = iterator.next();
			Beat currBeat;


			while (iterator.hasNext()) {
				currBeat = iterator.next();

				if (currBeat.timeMs - prevBeat.timeMs < minTimeBetween) {

					// keep the stronger beat
					Beat b = prevBeat.energy < currBeat.energy ? currBeat : prevBeat;
					result.add(b);

				} else 
					prevBeat = currBeat;

			}
			
			return result;

		}


	}
