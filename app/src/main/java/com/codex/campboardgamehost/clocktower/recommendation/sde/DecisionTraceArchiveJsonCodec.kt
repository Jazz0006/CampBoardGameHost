1: package com.codex.campboardgamehost.clocktower.recommendation.sde
2: 
3: import com.codex.campboardgamehost.clocktower.domain.AbilityState
4: import com.codex.campboardgamehost.clocktower.domain.RoleId
5: import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
6: import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
7: import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
8: import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
9: import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
10: import java.math.BigInteger
11: import org.json.JSONArray
12: import org.json.JSONObject
13: 
14: /**
15:  * Strict current-format persistence codec for the immutable DecisionTrace replay archive.
16:  *
17:  * Persisted traces remain diagnostic snapshots. Canonical historical facts are referenced through
18:  * Global history-prefix identities and remain owned by ActionFactTimeline / EpistemicObservationLog.
19:  */
20: internal object DecisionTraceArchiveJsonCodec {
21:     const val CURRENT_FORMAT_VERSION: Int = 1
22: 
23:     fun encode(archive: DecisionTraceArchive): String =
24:         JSONObject().apply {
25:             put("formatVersion", CURRENT_FORMAT_VERSION)
26:             put("traces", JSONArray().apply {
27:                 archive.traces.forEach { put(encodeTrace(it)) }
28:             })
29:         }.toString()
30: 
31:     fun decode(raw: String): DecisionTraceArchive {
32:         require(raw.isNotBlank()) { "DecisionTrace archive payload cannot be blank." }
33:         return try {
34:             decodeRoot(JSONObject(raw))
35:         } catch (error: IllegalArgumentException) {
36:             throw error
37:         } catch (error: Exception) {
38:             throw IllegalArgumentException("Malformed DecisionTrace archive payload.", error)
39:         }
40:     }
41: 
42:     private fun decodeRoot(root: JSONObject): DecisionTraceArchive {
43:         root.requireExactKeys("formatVersion", "traces")
44:         val version = root.requiredInt("formatVersion")
45:         require(version == CURRENT_FORMAT_VERSION) {
46:             "Unsupported DecisionTrace archive format version " + version + "."
47:         }
48:         return DecisionTraceArchive(
49:             root.requiredArray("traces").mapObjects("traces", ::decodeTrace),
50:         )
51:     }
52: 
53:     private fun encodeTrace(trace: DecisionTrace): JSONObject = JSONObject().apply {
54:         put("schemaVersion", trace.schemaVersion)
55:         put("evidenceCheckpoint", trace.evidenceCheckpoint.value)
56:         put("decisionId", trace.decisionId)
57:         put("lifecycleStage", encodeLifecycle(trace.lifecycleStage))
58:         put("sourceRevision", encodeRevision(trace.sourceRevision))
59:         put("historyPrefix", encodeGlobalPrefix(trace.historyPrefixRef))
60:         put("legalCandidateIds", strings(trace.legalCandidateIds))
61:         put("featureEvaluation", encodeFeatureEvaluation(trace.featureEvaluation))
62:         put("policySnapshot", encodePolicySnapshot(trace.policySnapshot))
63:         put("policySelection", trace.policySelection?.let(::encodePolicySelection) ?: JSONObject.NULL)
64:         put("actualChoice", encodeActualChoice(trace.actualChoice))
65:     }
66: 
67:     private fun decodeTrace(json: JSONObject): DecisionTrace {
68:         json.requireExactKeys(
69:             "schemaVersion",
70:             "evidenceCheckpoint",
71:             "decisionId",
72:             "lifecycleStage",
73:             "sourceRevision",
74:             "historyPrefix",
75:             "legalCandidateIds",
76:             "featureEvaluation",
77:             "policySnapshot",
78:             "policySelection",
79:             "actualChoice",
80:         )
81:         val persistedSchemaVersion = json.requiredInt("schemaVersion")
82:         require(
83:             persistedSchemaVersion == DecisionTrace.LEGACY_SCHEMA_VERSION ||
84:                 persistedSchemaVersion == DecisionTrace.CURRENT_SCHEMA_VERSION,
85:         ) {
86:             "Unsupported DecisionTrace schema version " + persistedSchemaVersion + "."
87:         }
88:         return DecisionTrace(
89:             schemaVersion = DecisionTrace.CURRENT_SCHEMA_VERSION,
90:             evidenceCheckpoint = EvidenceCheckpointId(json.requiredNonBlankString("evidenceCheckpoint")),
91:             decisionId = json.requiredNonBlankString("decisionId"),
92:             lifecycleStage = decodeLifecycle(json.requiredObject("lifecycleStage")),
93:             sourceRevision = decodeRevision(json.requiredObject("sourceRevision")),
94:             historyPrefixRef = decodeGlobalPrefix(json.requiredObject("historyPrefix")),
95:             legalCandidateIds = json.requiredStringList("legalCandidateIds"),
96:             featureEvaluation = decodeFeatureEvaluation(
97:                 json.requiredObject("featureEvaluation"),
98:                 persistedSchemaVersion,
99:             ),
100:             policySnapshot = decodePolicySnapshot(json.requiredObject("policySnapshot")),
101:             policySelection = json.requiredNullableObject("policySelection")?.let(::decodePolicySelection),
102:             actualChoice = decodeActualChoice(json.requiredObject("actualChoice")),
103:         )
104:     }
105: 
106:     private fun encodeLifecycle(stage: SdeDecisionLifecycleStage): JSONObject =
107:         when (stage) {
108:             SdeDecisionLifecycleStage.SetupPrecommit -> JSONObject().apply {
109:                 put("kind", "setupPrecommit")
110:             }
111:             is SdeDecisionLifecycleStage.Interaction -> JSONObject().apply {
112:                 put("kind", "interaction")
113:                 put("phase", stage.phase.name)
114:                 put("round", stage.round)
115:                 put("sequence", stage.sequence)
116:             }
117:         }
118: 
119:     private fun decodeLifecycle(json: JSONObject): SdeDecisionLifecycleStage =
120:         when (json.requiredNonBlankString("kind")) {
121:             "setupPrecommit" -> {
122:                 json.requireExactKeys("kind")
123:                 SdeDecisionLifecycleStage.SetupPrecommit
124:             }
125:             "interaction" -> {
126:                 json.requireExactKeys("kind", "phase", "round", "sequence")
127:                 SdeDecisionLifecycleStage.Interaction(
128:                     phase = json.requiredEnum("phase"),
129:                     round = json.requiredInt("round"),
130:                     sequence = json.requiredInt("sequence"),
131:                 )
132:             }
133:             else -> throw IllegalArgumentException("Unsupported DecisionTrace lifecycle kind.")
134:         }
135: 
136:     private fun encodeRevision(revision: InformationDecisionRevision): JSONObject =
137:         JSONObject().apply {
138:             put("gameStateRevision", revision.gameStateRevision)
139:             put("playerInputRevision", revision.playerInputRevision)
140:         }
141: 
142:     private fun decodeRevision(json: JSONObject): InformationDecisionRevision {
143:         json.requireExactKeys("gameStateRevision", "playerInputRevision")
144:         return InformationDecisionRevision(
145:             gameStateRevision = json.requiredLong("gameStateRevision"),
146:             playerInputRevision = json.requiredLong("playerInputRevision"),
147:         )
148:     }
149: 
150:     private fun encodeGlobalPrefix(prefix: SdeHistoricalPrefixRef): JSONObject {
151:         val global = prefix as? SdeHistoricalPrefixRef.Global
152:             ?: throw IllegalArgumentException(
153:                 "Only traces bound to a canonical global history prefix can be persisted.",
154:             )
155:         return JSONObject().apply {
156:             put("kind", "global")
157:             put("gameId", global.gameId)
158:             put("actionRefs", JSONArray().apply {
159:                 global.actionRefs.forEach { ref ->
160:                     put(JSONObject().apply {
161:                         put("actionId", ref.actionId)
162:                         put("globalSequence", ref.globalSequence)
163:                     })
164:                 }
165:             })
166:             put("observationRefs", JSONArray().apply {
167:                 global.observationRefs.forEach { ref ->
168:                     put(JSONObject().apply {
169:                         put("recordId", ref.recordId)
170:                         put("globalSequence", ref.globalSequence)
171:                     })
172:                 }
173:             })
174:         }
175:     }
176: 
177:     private fun decodeGlobalPrefix(json: JSONObject): SdeHistoricalPrefixRef.Global {
178:         json.requireExactKeys("kind", "gameId", "actionRefs", "observationRefs")
179:         require(json.requiredNonBlankString("kind") == "global") {
180:             "Persisted DecisionTrace requires a canonical global history prefix."
181:         }
182:         return SdeHistoricalPrefixRef.Global(
183:             gameId = json.requiredNonBlankString("gameId"),
184:             actionRefs = json.requiredArray("actionRefs").mapObjects("actionRefs") { ref ->
185:                 ref.requireExactKeys("actionId", "globalSequence")
186:                 SdeHistoricalActionRef(
187:                     actionId = ref.requiredNonBlankString("actionId"),
188:                     globalSequence = ref.requiredLong("globalSequence"),
189:                 )
190:             },
191:             observationRefs = json.requiredArray("observationRefs").mapObjects("observationRefs") { ref ->
192:                 ref.requireExactKeys("recordId", "globalSequence")
193:                 SdeHistoricalObservationRef(
194:                     recordId = ref.requiredNonBlankString("recordId"),
195:                     globalSequence = ref.requiredLong("globalSequence"),
196:                 )
197:             },
198:         )
199:     }
200: 
201:     private fun encodeFeatureEvaluation(evaluation: DecisionFeatureEvaluation): JSONObject =
202:         when (evaluation) {
203:             is DecisionFeatureEvaluation.Ready -> JSONObject().apply {
204:                 put("kind", "ready")
205:                 put("candidates", JSONArray().apply {
206:                     evaluation.candidates.forEach { candidate ->
207:                         put(JSONObject().apply {
208:                             put("candidateId", candidate.candidateId)
209:                             put("features", encodeFeatures(candidate.features))
210:                         })
211:                     }
212:                 })
213:             }
214:             is DecisionFeatureEvaluation.Deferred -> JSONObject().apply {
215:                 put("kind", "deferred")
216:                 put("candidateIds", strings(evaluation.candidateIds))
217:                 put(
218:                     "missingCapabilities",
219:                     strings(evaluation.missingCapabilities.map { it.name }.sorted()),
220:                 )
221:             }
222:         }
223: 
224:     private fun decodeFeatureEvaluation(
225:         json: JSONObject,
226:         traceSchemaVersion: Int,
227:     ): DecisionFeatureEvaluation =
228:         when (json.requiredNonBlankString("kind")) {
229:             "ready" -> {
230:                 json.requireExactKeys("kind", "candidates")
231:                 DecisionFeatureEvaluation.Ready(
232:                     candidates = json.requiredArray("candidates").mapObjects("candidates") { candidate ->
233:                         candidate.requireExactKeys("candidateId", "features")
234:                         CandidateDecisionFeatures(
235:                             candidateId = candidate.requiredNonBlankString("candidateId"),
236:                             features = decodeFeatures(
237:                                 candidate.requiredObject("features"),
238:                                 traceSchemaVersion,
239:                             ),
240:                         )
241:                     },
242:                 )
243:             }
244:             "deferred" -> {
245:                 json.requireExactKeys("kind", "candidateIds", "missingCapabilities")
246:                 DecisionFeatureEvaluation.Deferred(
247:                     candidateIds = json.requiredStringList("candidateIds"),
248:                     missingCapabilities =
249:                         json.requiredEnumSet<EpistemicEvaluationCapability>("missingCapabilities"),
250:                 )
251:             }
252:             else -> throw IllegalArgumentException("Unsupported DecisionFeatureEvaluation kind.")
253:         }
254: 
255:     private fun encodeFeatures(features: DecisionFeatures): JSONObject = JSONObject().apply {
256:         put("strategic", encodeProjection(features.strategic, ::encodeStrategicFeatures))
257:         put(
258:             "confirmationChainImpact",
259:             encodeProjection(features.confirmationChainImpact, ::encodeConfirmationFeatures),
260:         )
261:         put(
262:             "healthyInformationUtility",
263:             encodeProjection(features.healthyInformationUtility, ::encodeHealthyFeatures),
264:         )
265:         put("truthCredibility", encodeProjection(features.truthCredibility, ::encodeTruthCredibility))
266:         put(
267:             "roleFunctionExposure",
268:             encodeProjection(features.roleFunctionExposure, ::encodeRoleFunctionExposure),
269:         )
270:         put("semanticTruth", encodeProjection(features.semanticTruth) { it.name })
271:         put(
272:             "impairedNarrative",
273:             encodeProjection(features.impairedNarrative, ::encodeImpairedNarrative),
274:         )
275:         put("bluffNarrative", encodeProjection(features.bluffNarrative, ::encodeBluffNarrative))
276:         put("relationships", encodeProjection(features.relationships, ::encodeRelationships))
277:         put(
278:             "futureFlexibility",
279:             encodeProjection(features.futureFlexibility, ::encodeFutureFlexibility),
280:         )
281:     }
282: 
283:     private fun decodeFeatures(
284:         json: JSONObject,
285:         traceSchemaVersion: Int,
286:     ): DecisionFeatures {
287:         json.requireExactKeys(
288:             "strategic",
289:             "confirmationChainImpact",
290:             "healthyInformationUtility",
291:             "truthCredibility",
292:             "roleFunctionExposure",
293:             "semanticTruth",
294:             "impairedNarrative",
295:             "bluffNarrative",
296:             "relationships",
297:             "futureFlexibility",
298:         )
299:         return DecisionFeatures(
300:             strategic =
301:                 decodeObjectProjection(json.requiredObject("strategic"), ::decodeStrategicFeatures),
302:             confirmationChainImpact =
303:                 decodeObjectProjection(
304:                     json.requiredObject("confirmationChainImpact"),
305:                     ::decodeConfirmationFeatures,
306:                 ),
307:             healthyInformationUtility =
308:                 decodeObjectProjection(
309:                     json.requiredObject("healthyInformationUtility"),
310:                     ::decodeHealthyFeatures,
311:                 ),
312:             truthCredibility =
313:                 decodeObjectProjection(
314:                     json.requiredObject("truthCredibility"),
315:                 ) { value ->
316:                     decodeTruthCredibility(value, traceSchemaVersion)
317:                 },
318:             roleFunctionExposure =
319:                 decodeObjectProjection(
320:                     json.requiredObject("roleFunctionExposure"),
321:                     ::decodeRoleFunctionExposure,
322:                 ),
323:             semanticTruth =
324:                 decodeStringProjection<SemanticTruth>(json.requiredObject("semanticTruth")),
325:             impairedNarrative =
326:                 decodeObjectProjection(
327:                     json.requiredObject("impairedNarrative"),
328:                     ::decodeImpairedNarrative,
329:                 ),
330:             bluffNarrative =
331:                 decodeObjectProjection(
332:                     json.requiredObject("bluffNarrative"),
333:                     ::decodeBluffNarrative,
334:                 ),
335:             relationships =
336:                 decodeObjectProjection(
337:                     json.requiredObject("relationships"),
338:                     ::decodeRelationships,
339:                 ),
340:             futureFlexibility =
341:                 decodeObjectProjection(
342:                     json.requiredObject("futureFlexibility"),
343:                     ::decodeFutureFlexibility,
344:                 ),
345:         )
346:     }
347: 
348:     private fun <T> encodeProjection(
349:         projection: FeatureProjection<T>,
350:         encodeValue: (T) -> Any,
351:     ): JSONObject =
352:         when (projection) {
353:             is FeatureProjection.Projected -> JSONObject().apply {
354:                 put("kind", "projected")
355:                 put("value", encodeValue(projection.value))
356:             }
357:             is FeatureProjection.Unavailable -> JSONObject().apply {
358:                 put("kind", "unavailable")
359:                 put("reason", projection.reason.name)
360:             }
361:         }
362: 
363:     private fun <T> decodeObjectProjection(
364:         json: JSONObject,
365:         decodeValue: (JSONObject) -> T,
366:     ): FeatureProjection<T> =
367:         when (json.requiredNonBlankString("kind")) {
368:             "projected" -> {
369:                 json.requireExactKeys("kind", "value")
370:                 FeatureProjection.Projected(decodeValue(json.requiredObject("value")))
371:             }
372:             "unavailable" -> {
373:                 json.requireExactKeys("kind", "reason")
374:                 FeatureProjection.Unavailable(json.requiredEnum("reason"))
375:             }
376:             else -> throw IllegalArgumentException("Unsupported feature projection kind.")
377:         }
378: 
379:     private inline fun <reified T : Enum<T>> decodeStringProjection(
380:         json: JSONObject,
381:     ): FeatureProjection<T> =
382:         when (json.requiredNonBlankString("kind")) {
383:             "projected" -> {
384:                 json.requireExactKeys("kind", "value")
385:                 FeatureProjection.Projected(json.requiredEnum("value"))
386:             }
387:             "unavailable" -> {
388:                 json.requireExactKeys("kind", "reason")
389:                 FeatureProjection.Unavailable(json.requiredEnum("reason"))
390:             }
391:             else -> throw IllegalArgumentException("Unsupported feature projection kind.")
392:         }
393: 
394:     private fun encodeStrategicFeatures(value: StrategicDecisionFeatures): JSONObject =
395:         JSONObject().apply {
396:             put("demonCoverRetention", encodeRatio(value.demonCoverRetention))
397:             put("evilTopologyRetention", encodeRatio(value.evilTopologyRetention))
398:             put("evilCoverRetention", encodeRatio(value.evilCoverRetention))
399:             put("forcedGoodFraction", encodeRatio(value.forcedGoodFraction))
400:             put("forcedEvilFraction", encodeRatio(value.forcedEvilFraction))
401:             put("forcedGoodSeats", ints(value.forcedGoodSeats.sorted()))
402:             put("forcedEvilSeats", ints(value.forcedEvilSeats.sorted()))
403:         }
404: 
405:     private fun decodeStrategicFeatures(json: JSONObject): StrategicDecisionFeatures {
406:         json.requireExactKeys(
407:             "demonCoverRetention",
408:             "evilTopologyRetention",
409:             "evilCoverRetention",
410:             "forcedGoodFraction",
411:             "forcedEvilFraction",
412:             "forcedGoodSeats",
413:             "forcedEvilSeats",
414:         )
415:         return StrategicDecisionFeatures(
416:             demonCoverRetention = decodeRatio(json.requiredObject("demonCoverRetention")),
417:             evilTopologyRetention = decodeRatio(json.requiredObject("evilTopologyRetention")),
418:             evilCoverRetention = decodeRatio(json.requiredObject("evilCoverRetention")),
419:             forcedGoodFraction = decodeDefinedRatio(json.requiredObject("forcedGoodFraction")),
420:             forcedEvilFraction = decodeDefinedRatio(json.requiredObject("forcedEvilFraction")),
421:             forcedGoodSeats =
422:                 json.requiredIntList("forcedGoodSeats").toSetStrict("forcedGoodSeats"),
423:             forcedEvilSeats =
424:                 json.requiredIntList("forcedEvilSeats").toSetStrict("forcedEvilSeats"),
425:         )
426:     }
427: 
428:     private fun encodeRatio(value: StrategicRatio): JSONObject =
429:         when (value) {
430:             is StrategicRatio.Defined -> JSONObject().apply {
431:                 put("kind", "defined")
432:                 put("numerator", value.numerator)
433:                 put("denominator", value.denominator)
434:             }
435:             StrategicRatio.Undefined -> JSONObject().apply {
436:                 put("kind", "undefined")
437:             }
438:         }
439: 
440:     private fun decodeRatio(json: JSONObject): StrategicRatio =
441:         when (json.requiredNonBlankString("kind")) {
442:             "defined" -> decodeDefinedRatio(json)
443:             "undefined" -> {
444:                 json.requireExactKeys("kind")
445:                 StrategicRatio.Undefined
446:             }
447:             else -> throw IllegalArgumentException("Unsupported strategic ratio kind.")
448:         }
449: 
450:     private fun decodeDefinedRatio(json: JSONObject): StrategicRatio.Defined {
451:         json.requireExactKeys("kind", "numerator", "denominator")
452:         require(json.requiredNonBlankString("kind") == "defined") {
453:             "Expected defined strategic ratio."
454:         }
455:         return StrategicRatio.Defined(
456:             numerator = json.requiredInt("numerator"),
457:             denominator = json.requiredInt("denominator"),
458:         )
459:     }
460: 
461:     private fun encodeConfirmationFeatures(value: ConfirmationChainFeatures): JSONObject =
462:         JSONObject().apply {
463:             put("candidateRemovedExactWorldCount", value.candidateRemovedExactWorldCount.toString())
464:             put(
465:                 "candidateRemovedStrategicWorldKeys",
466:                 strategicWorldKeys(value.candidateRemovedStrategicWorldKeys),
467:             )
468:             put("candidateRemovedDemonSeats", ints(value.candidateRemovedDemonSeats.sorted()))
469:             put("candidateChannel", encodeConfirmationChannel(value.candidateChannel))
470:             put("historicalObservationImpacts", JSONArray().apply {
471:                 value.historicalObservationImpacts.forEach { put(encodeHistoricalImpact(it)) }
472:             })
473:         }
474: 
475:     private fun decodeConfirmationFeatures(json: JSONObject): ConfirmationChainFeatures {
476:         json.requireExactKeys(
477:             "candidateRemovedExactWorldCount",
478:             "candidateRemovedStrategicWorldKeys",
479:             "candidateRemovedDemonSeats",
480:             "candidateChannel",
481:             "historicalObservationImpacts",
482:         )
483:         return ConfirmationChainFeatures(
484:             candidateRemovedExactWorldCount =
485:                 json.requiredBigInteger("candidateRemovedExactWorldCount"),
486:             candidateRemovedStrategicWorldKeys =
487:                 json.requiredStrategicWorldKeySet("candidateRemovedStrategicWorldKeys"),
488:             candidateRemovedDemonSeats =
489:                 json.requiredIntList("candidateRemovedDemonSeats")
490:                     .toSetStrict("candidateRemovedDemonSeats"),
491:             candidateChannel = decodeConfirmationChannel(json.requiredObject("candidateChannel")),
492:             historicalObservationImpacts =
493:                 json.requiredArray("historicalObservationImpacts")
494:                     .mapObjects("historicalObservationImpacts", ::decodeHistoricalImpact),
495:         )
496:     }
497: 
498:     private fun encodeHistoricalImpact(
499:         value: HistoricalObservationConfirmationImpact,
500:     ): JSONObject =
501:         JSONObject().apply {
502:             put("provenance", JSONObject().apply {
503:                 put("observationRef", JSONObject().apply {
504:                     put("recordId", value.provenance.observationRef.recordId)
505:                     put("globalSequence", value.provenance.observationRef.globalSequence)
506:                 })
507:                 put("sourceSeat", value.provenance.sourceSeat ?: JSONObject.NULL)
508:                 put("sourceAbility", value.provenance.sourceAbility?.value ?: JSONObject.NULL)
509:                 put("channel", encodeConfirmationChannel(value.provenance.channel))
510:             })
511:             put("relation", value.relation.name)
512:             put("restoration", JSONObject().apply {
513:                 put(
514:                     "restoredExactWorldCount",
515:                     value.restoration.restoredExactWorldCount.toString(),
516:                 )
517:                 put(
518:                     "restoredStrategicWorldKeys",
519:                     strategicWorldKeys(value.restoration.restoredStrategicWorldKeys),
520:                 )
521:                 put("restoredDemonSeats", ints(value.restoration.restoredDemonSeats.sorted()))
522:             })
523:             put("authenticatesDistinctSource", value.authenticatesDistinctSource)
524:             put(
525:                 "wasIndependentlyConstrainingBefore",
526:                 value.wasIndependentlyConstrainingBefore,
527:             )
528:         }
529: 
530:     private fun decodeHistoricalImpact(
531:         json: JSONObject,
532:     ): HistoricalObservationConfirmationImpact {
533:         json.requireExactKeys(
534:             "provenance",
535:             "relation",
536:             "restoration",
537:             "authenticatesDistinctSource",
538:             "wasIndependentlyConstrainingBefore",
539:         )
540:         val provenance = json.requiredObject("provenance")
541:         provenance.requireExactKeys("observationRef", "sourceSeat", "sourceAbility", "channel")
542:         val observationRef = provenance.requiredObject("observationRef")
543:         observationRef.requireExactKeys("recordId", "globalSequence")
544:         val restoration = json.requiredObject("restoration")
545:         restoration.requireExactKeys(
546:             "restoredExactWorldCount",
547:             "restoredStrategicWorldKeys",
548:             "restoredDemonSeats",
549:         )
550:         return HistoricalObservationConfirmationImpact(
551:             provenance = ConfirmationObservationProvenance(
552:                 observationRef = SdeHistoricalObservationRef(
553:                     recordId = observationRef.requiredNonBlankString("recordId"),
554:                     globalSequence = observationRef.requiredLong("globalSequence"),
555:                 ),
556:                 sourceSeat = provenance.requiredNullableInt("sourceSeat"),
557:                 sourceAbility =
558:                     provenance.requiredNullableString("sourceAbility")?.let(::RoleId),
559:                 channel = decodeConfirmationChannel(provenance.requiredObject("channel")),
560:             ),
561:             relation = json.requiredEnum("relation"),
562:             restoration = ConfirmationAmbiguityRestoration(
563:                 restoredExactWorldCount =
564:                     restoration.requiredBigInteger("restoredExactWorldCount"),
565:                 restoredStrategicWorldKeys =
566:                     restoration.requiredStrategicWorldKeySet("restoredStrategicWorldKeys"),
567:                 restoredDemonSeats =
568:                     restoration.requiredIntList("restoredDemonSeats")
569:                         .toSetStrict("restoredDemonSeats"),
570:             ),
571:             authenticatesDistinctSource =
572:                 json.requiredBoolean("authenticatesDistinctSource"),
573:             wasIndependentlyConstrainingBefore =
574:                 json.requiredBoolean("wasIndependentlyConstrainingBefore"),
575:         )
576:     }
577: 
578:     private fun encodeConfirmationChannel(value: ConfirmationChannelRef): JSONObject =
579:         when (value) {
580:             is ConfirmationChannelRef.Source -> JSONObject().apply {
581:                 put("kind", "source")
582:                 put("sourceSeat", value.sourceSeat ?: JSONObject.NULL)
583:                 put("sourceAbility", value.sourceAbility?.value ?: JSONObject.NULL)
584:             }
585:             is ConfirmationChannelRef.Interaction -> JSONObject().apply {
586:                 put("kind", "interaction")
587:                 put("interactionId", value.interactionId)
588:             }
589:             is ConfirmationChannelRef.Observation -> JSONObject().apply {
590:                 put("kind", "observation")
591:                 put("observationRecordId", value.observationRecordId)
592:             }
593:         }
594: 
595:     private fun decodeConfirmationChannel(json: JSONObject): ConfirmationChannelRef =
596:         when (json.requiredNonBlankString("kind")) {
597:             "source" -> {
598:                 json.requireExactKeys("kind", "sourceSeat", "sourceAbility")
599:                 ConfirmationChannelRef.Source(
600:                     sourceSeat = json.requiredNullableInt("sourceSeat"),
601:                     sourceAbility =
602:                         json.requiredNullableString("sourceAbility")?.let(::RoleId),
603:                 )
604:             }
605:             "interaction" -> {
606:                 json.requireExactKeys("kind", "interactionId")
607:                 ConfirmationChannelRef.Interaction(
608:                     json.requiredNonBlankString("interactionId"),
609:                 )
610:             }
611:             "observation" -> {
612:                 json.requireExactKeys("kind", "observationRecordId")
613:                 ConfirmationChannelRef.Observation(
614:                     json.requiredNonBlankString("observationRecordId"),
615:                 )
616:             }
617:             else -> throw IllegalArgumentException("Unsupported confirmation channel kind.")
618:         }
619: 
620:     private fun encodeHealthyFeatures(value: HealthyInformationUtilityFeatures): JSONObject =
621:         JSONObject().apply {
622:             put("usableBefore", healthyRoutes(value.usableHealthyRouteRefsBefore))
623:             put("independentBefore", healthyRoutes(value.independentHealthyRouteRefsBefore))
624:             put("usableAfter", healthyRoutes(value.usableHealthyRouteRefsAfter))
625:             put("independentAfter", healthyRoutes(value.independentHealthyRouteRefsAfter))
626:             put("newlyRedundant", healthyRoutes(value.newlyRedundantHealthyRouteRefs))
627:             put("contradicted", healthyRoutes(value.contradictedHealthyRouteRefs))
628:             put(
629:                 "currentCandidate",
630:                 value.currentCandidateHealthyRouteRef?.let(::encodeHealthyRoute)
631:                     ?: JSONObject.NULL,
632:             )
633:         }
634: 
635:     private fun decodeHealthyFeatures(json: JSONObject): HealthyInformationUtilityFeatures {
636:         json.requireExactKeys(
637:             "usableBefore",
638:             "independentBefore",
639:             "usableAfter",
640:             "independentAfter",
641:             "newlyRedundant",
642:             "contradicted",
643:             "currentCandidate",
644:         )
645:         val current =
646:             json.requiredNullableObject("currentCandidate")?.let(::decodeHealthyRoute)
647:         require(current == null || current is HealthyInformationRouteRef.CurrentCandidate) {
648:             "Healthy current candidate route must use current-candidate identity."
649:         }
650:         return HealthyInformationUtilityFeatures(
651:             usableHealthyRouteRefsBefore =
652:                 json.requiredHealthyHistoricalRouteSet("usableBefore"),
653:             independentHealthyRouteRefsBefore =
654:                 json.requiredHealthyHistoricalRouteSet("independentBefore"),
655:             usableHealthyRouteRefsAfter =
656:                 json.requiredHealthyRouteSet("usableAfter"),
657:             independentHealthyRouteRefsAfter =
658:                 json.requiredHealthyRouteSet("independentAfter"),
659:             newlyRedundantHealthyRouteRefs =
660:                 json.requiredHealthyHistoricalRouteSet("newlyRedundant"),
661:             contradictedHealthyRouteRefs =
662:                 json.requiredHealthyHistoricalRouteSet("contradicted"),
663:             currentCandidateHealthyRouteRef =
664:                 current as? HealthyInformationRouteRef.CurrentCandidate,
665:         )
666:     }
667: 
668:     private fun encodeHealthyRoute(value: HealthyInformationRouteRef): JSONObject =
669:         when (value) {
670:             is HealthyInformationRouteRef.HistoricalObservation -> JSONObject().apply {
671:                 put("kind", "historical")
672:                 put("recordId", value.recordId)
673:                 put("recipientSeat", value.recipientSeat)
674:             }
675:             is HealthyInformationRouteRef.CurrentCandidate -> JSONObject().apply {
676:                 put("kind", "current")
677:                 put("candidateId", value.candidateId)
678:                 put("recipientSeat", value.recipientSeat)
679:             }
680:         }
681: 
682:     private fun decodeHealthyRoute(json: JSONObject): HealthyInformationRouteRef =
683:         when (json.requiredNonBlankString("kind")) {
684:             "historical" -> {
685:                 json.requireExactKeys("kind", "recordId", "recipientSeat")
686:                 HealthyInformationRouteRef.HistoricalObservation(
687:                     recordId = json.requiredNonBlankString("recordId"),
688:                     recipientSeat = json.requiredInt("recipientSeat"),
689:                 )
690:             }
691:             "current" -> {
692:                 json.requireExactKeys("kind", "candidateId", "recipientSeat")
693:                 HealthyInformationRouteRef.CurrentCandidate(
694:                     candidateId = json.requiredNonBlankString("candidateId"),
695:                     recipientSeat = json.requiredInt("recipientSeat"),
696:                 )
697:             }
698:             else -> throw IllegalArgumentException(
699:                 "Unsupported healthy-information route kind.",
700:             )
701:         }
702: 
703:     private fun encodeTruthCredibility(value: TruthCredibilityFeatures): JSONObject =
704:         JSONObject().apply {
705:             put(
706:                 "truthDangerSources",
707:                 JSONArray().apply {
708:                     value.truthDangerSources
709:                         .sortedWith(
710:                             compareBy<TruthDangerSourceImpact>(
711:                                 { it.source.sourceSeat ?: Int.MIN_VALUE },
712:                                 { it.source.sourceAbility?.value ?: "" },
713:                             ),
714:                         )
715:                         .forEach { put(encodeTruthDangerSourceImpact(it)) }
716:                 },
717:             )
718:             put(
719:                 "credibilityDisruptions",
720:                 JSONArray().apply {
721:                     value.credibilityDisruptions
722:                         .sortedWith(
723:                             compareBy<CredibilityDisruptionImpact>(
724:                                 { it.committedInputRef.inputId },
725:                                 { it.committedInputRef.ownerId },
726:                                 { it.committedInputRef.kind.name },
727:                                 { it.affectedSource.sourceSeat ?: Int.MIN_VALUE },
728:                                 { it.affectedSource.sourceAbility?.value ?: "" },
729:                                 { it.mechanism.name },
730:                             ),
731:                         )
732:                         .forEach { put(encodeCredibilityDisruptionImpact(it)) }
733:                 },
734:             )
735:             put(
736:                 "unresolvedSourceRefs",
737:                 JSONArray().apply {
738:                     value.unresolvedSourceRefs
739:                         .sortedWith(
740:                             compareBy<ConfirmationChannelRef.Source>(
741:                                 { it.sourceSeat ?: Int.MIN_VALUE },
742:                                 { it.sourceAbility?.value ?: "" },
743:                             ),
744:                         )
745:                         .forEach { put(encodeConfirmationChannel(it)) }
746:                 },
747:             )
748:             put("truthDangerReasonCodes", strings(value.truthDangerReasonCodes.sorted()))
749:             put(
750:                 "credibilityDisruptionReasonCodes",
751:                 strings(value.credibilityDisruptionReasonCodes.sorted()),
752:             )
753:         }
754: 
755:     private fun decodeTruthCredibility(
756:         json: JSONObject,
757:         traceSchemaVersion: Int,
758:     ): TruthCredibilityFeatures =
759:         when (traceSchemaVersion) {
760:             DecisionTrace.LEGACY_SCHEMA_VERSION -> {
761:                 json.requireExactKeys(
762:                     "truthDangerReasonCodes",
763:                     "credibilityDisruptionReasonCodes",
764:                 )
765:                 TruthCredibilityFeatures(
766:                     truthDangerReasonCodes =
767:                         json.requiredStringSet("truthDangerReasonCodes"),
768:                     credibilityDisruptionReasonCodes =
769:                         json.requiredStringSet("credibilityDisruptionReasonCodes"),
770:                 )
771:             }
772: 
773:             DecisionTrace.CURRENT_SCHEMA_VERSION -> {
774:                 json.requireExactKeys(
775:                     "truthDangerSources",
776:                     "credibilityDisruptions",
777:                     "unresolvedSourceRefs",
778:                     "truthDangerReasonCodes",
779:                     "credibilityDisruptionReasonCodes",
780:                 )
781:                 TruthCredibilityFeatures(
782:                     truthDangerSources =
783:                         json.requiredArray("truthDangerSources")
784:                             .mapObjects("truthDangerSources", ::decodeTruthDangerSourceImpact)
785:                             .toSetStrict("truthDangerSources"),
786:                     credibilityDisruptions =
787:                         json.requiredArray("credibilityDisruptions")
788:                             .mapObjects(
789:                                 "credibilityDisruptions",
790:                                 ::decodeCredibilityDisruptionImpact,
791:                             )
792:                             .toSetStrict("credibilityDisruptions"),
793:                     unresolvedSourceRefs =
794:                         json.requiredArray("unresolvedSourceRefs")
795:                             .mapObjects("unresolvedSourceRefs", ::decodeConfirmationSource)
796:                             .toSetStrict("unresolvedSourceRefs"),
797:                     truthDangerReasonCodes =
798:                         json.requiredStringSet("truthDangerReasonCodes"),
799:                     credibilityDisruptionReasonCodes =
800:                         json.requiredStringSet("credibilityDisruptionReasonCodes"),
801:                 )
802:             }
803: 
804:             else -> throw IllegalArgumentException(
805:                 "Unsupported DecisionTrace truth/credibility schema version $traceSchemaVersion.",
806:             )
807:         }
808: 
809:     private fun encodeTruthDangerSourceImpact(
810:         value: TruthDangerSourceImpact,
811:     ): JSONObject =
812:         JSONObject().apply {
813:             put("source", encodeConfirmationChannel(value.source))
814:             put("exactWorldReduction", value.exactWorldReduction.toString())
815:             put(
816:                 "strategicWorldKeysRemoved",
817:                 strategicWorldKeys(value.strategicWorldKeysRemoved),
818:             )
819:             put("demonSeatsRemoved", ints(value.demonSeatsRemoved.sorted()))
820:         }
821: 
822:     private fun decodeTruthDangerSourceImpact(
823:         json: JSONObject,
824:     ): TruthDangerSourceImpact {
825:         json.requireExactKeys(
826:             "source",
827:             "exactWorldReduction",
828:             "strategicWorldKeysRemoved",
829:             "demonSeatsRemoved",
830:         )
831:         return TruthDangerSourceImpact(
832:             source = decodeConfirmationSource(json.requiredObject("source")),
833:             exactWorldReduction = json.requiredBigInteger("exactWorldReduction"),
834:             strategicWorldKeysRemoved =
835:                 json.requiredStrategicWorldKeySet("strategicWorldKeysRemoved"),
836:             demonSeatsRemoved =
837:                 json.requiredIntList("demonSeatsRemoved")
838:                     .toSetStrict("demonSeatsRemoved"),
839:         )
840:     }
841: 
842:     private fun encodeCredibilityDisruptionImpact(
843:         value: CredibilityDisruptionImpact,
844:     ): JSONObject =
845:         JSONObject().apply {
846:             put("committedInputRef", encodeCommittedDecisionInputRef(value.committedInputRef))
847:             put("affectedSource", encodeConfirmationChannel(value.affectedSource))
848:             put("mechanism", value.mechanism.name)
849:         }
850: 
851:     private fun decodeCredibilityDisruptionImpact(
852:         json: JSONObject,
853:     ): CredibilityDisruptionImpact {
854:         json.requireExactKeys("committedInputRef", "affectedSource", "mechanism")
855:         return CredibilityDisruptionImpact(
856:             committedInputRef =
857:                 decodeCommittedDecisionInputRef(json.requiredObject("committedInputRef")),
858:             affectedSource = decodeConfirmationSource(json.requiredObject("affectedSource")),
859:             mechanism = json.requiredEnum("mechanism"),
860:         )
861:     }
862: 
863:     private fun encodeCommittedDecisionInputRef(
864:         value: CommittedDecisionInputRef,
865:     ): JSONObject =
866:         JSONObject().apply {
867:             put("inputId", value.inputId)
868:             put("ownerId", value.ownerId)
869:             put("kind", value.kind.name)
870:         }
871: 
872:     private fun decodeCommittedDecisionInputRef(
873:         json: JSONObject,
874:     ): CommittedDecisionInputRef {
875:         json.requireExactKeys("inputId", "ownerId", "kind")
876:         return CommittedDecisionInputRef(
877:             inputId = json.requiredNonBlankString("inputId"),
878:             ownerId = json.requiredNonBlankString("ownerId"),
879:             kind = json.requiredEnum("kind"),
880:         )
881:     }
882: 
883:     private fun decodeConfirmationSource(
884:         json: JSONObject,
885:     ): ConfirmationChannelRef.Source =
886:         decodeConfirmationChannel(json) as? ConfirmationChannelRef.Source
887:             ?: throw IllegalArgumentException("Truth/credibility source must use source channel kind.")
888: 
889:     private fun encodeRoleFunctionExposure(
890:         value: RoleFunctionExposureFeatures,
891:     ): JSONObject =
892:         JSONObject().apply {
893:             put("direct", exposureTargets(value.directlyExposedTargets))
894:             put("already", exposureTargets(value.alreadyExposedTargets))
895:             put("new", exposureTargets(value.newlyExposedTargets))
896:             put(
897:                 "confirmationAmplified",
898:                 exposureTargets(value.confirmationAmplifiedTargets),
899:             )
900:             put("forced", exposureTargets(value.forcedExposureTargets))
901:             put("avoidable", exposureTargets(value.avoidableExposureTargets))
902:         }
903: 
904:     private fun decodeRoleFunctionExposure(
905:         json: JSONObject,
906:     ): RoleFunctionExposureFeatures {
907:         json.requireExactKeys(
908:             "direct",
909:             "already",
910:             "new",
911:             "confirmationAmplified",
912:             "forced",
913:             "avoidable",
914:         )
915:         return RoleFunctionExposureFeatures(
916:             directlyExposedTargets = json.requiredExposureTargetSet("direct"),
917:             alreadyExposedTargets = json.requiredExposureTargetSet("already"),
918:             newlyExposedTargets = json.requiredExposureTargetSet("new"),
919:             confirmationAmplifiedTargets =
920:                 json.requiredExposureTargetSet("confirmationAmplified"),
921:             forcedExposureTargets = json.requiredExposureTargetSet("forced"),
922:             avoidableExposureTargets = json.requiredExposureTargetSet("avoidable"),
923:         )
924:     }
925: 
926:     private fun encodeExposureTarget(
927:         value: RoleFunctionExposureTargetRef,
928:     ): JSONObject =
929:         JSONObject().apply {
930:             put("seat", value.seat)
931:             put("role", value.role.value)
932:             put("recipientSeat", value.recipientSeat)
933:             put("capability", value.capability.name)
934:         }
935: 
936:     private fun decodeExposureTarget(
937:         json: JSONObject,
938:     ): RoleFunctionExposureTargetRef {
939:         json.requireExactKeys("seat", "role", "recipientSeat", "capability")
940:         return RoleFunctionExposureTargetRef(
941:             seat = json.requiredInt("seat"),
942:             role = RoleId(json.requiredNonBlankString("role")),
943:             recipientSeat = json.requiredInt("recipientSeat"),
944:             capability = json.requiredEnum("capability"),
945:         )
946:     }
947: 
948:     private fun encodeImpairedNarrative(
949:         value: ImpairedNarrativeFeatures,
950:     ): JSONObject =
951:         JSONObject().apply {
952:             put("currentAbilityState", value.currentAbilityState.name)
953:             put("impairmentLifetime", value.impairmentLifetime.name)
954:             put(
955:                 "priorImpairedObservationIds",
956:                 strings(value.priorImpairedObservationIds.sorted()),
957:             )
958:             put(
959:                 "contradictoryPriorObservationIds",
960:                 strings(value.contradictoryPriorObservationIds.sorted()),
961:             )
962:             put("relation", value.relation.name)
963:             put("transitionNecessity", value.transitionNecessity.name)
964:             put("detectabilitySignal", value.detectabilitySignal.name)
965:         }
966: 
967:     private fun decodeImpairedNarrative(
968:         json: JSONObject,
969:     ): ImpairedNarrativeFeatures {
970:         json.requireExactKeys(
971:             "currentAbilityState",
972:             "impairmentLifetime",
973:             "priorImpairedObservationIds",
974:             "contradictoryPriorObservationIds",
975:             "relation",
976:             "transitionNecessity",
977:             "detectabilitySignal",
978:         )
979:         return ImpairedNarrativeFeatures(
980:             currentAbilityState = json.requiredEnum<AbilityState>("currentAbilityState"),
981:             impairmentLifetime = json.requiredEnum("impairmentLifetime"),
982:             priorImpairedObservationIds =
983:                 json.requiredStringSet("priorImpairedObservationIds"),
984:             contradictoryPriorObservationIds =
985:                 json.requiredStringSet("contradictoryPriorObservationIds"),
986:             relation = json.requiredEnum("relation"),
987:             transitionNecessity = json.requiredEnum("transitionNecessity"),
988:             detectabilitySignal = json.requiredEnum("detectabilitySignal"),
989:         )
990:     }
991: 
992:     private fun encodeBluffNarrative(value: BluffNarrativeFeatures): JSONObject =
993:         JSONObject().apply {
994:             put(
995:                 "claimBurdenReasonCodes",
996:                 strings(value.claimBurdenReasonCodes.sorted()),
997:             )
998:             put("narrativeRouteIds", strings(value.narrativeRouteIds.sorted()))
999:         }
1000:
1001:     private fun decodeBluffNarrative(json: JSONObject): BluffNarrativeFeatures {
1002:         json.requireExactKeys("claimBurdenReasonCodes", "narrativeRouteIds")
1003:         return BluffNarrativeFeatures(
1004:             claimBurdenReasonCodes =
1005:                 json.requiredStringSet("claimBurdenReasonCodes"),
1006:             narrativeRouteIds = json.requiredStringSet("narrativeRouteIds"),
1007:         )
1008:     }
1009: 
1010:     private fun encodeRelationships(value: DecisionRelationshipFeatures): JSONObject =
1011:         JSONObject().apply {
1012:             put(
1013:                 "collisionCandidateIds",
1014:                 strings(value.collisionCandidateIds.sorted()),
1015:             )
1016:             put("supportCandidateIds", strings(value.supportCandidateIds.sorted()))
1017:         }
1018: 
1019:     private fun decodeRelationships(
1020:         json: JSONObject,
1021:     ): DecisionRelationshipFeatures {
1022:         json.requireExactKeys("collisionCandidateIds", "supportCandidateIds")
1023:         return DecisionRelationshipFeatures(
1024:             collisionCandidateIds =
1025:                 json.requiredStringSet("collisionCandidateIds"),
1026:             supportCandidateIds = json.requiredStringSet("supportCandidateIds"),
1027:         )
1028:     }
1029: 
1030:     private fun encodeFutureFlexibility(
1031:         value: FutureFlexibilityFeatures,
1032:     ): JSONObject =
1033:         JSONObject().apply {
1034:             put("retainedRouteIds", strings(value.retainedRouteIds.sorted()))
1035:             put("lostRouteIds", strings(value.lostRouteIds.sorted()))
1036:             put("reasonCodes", strings(value.reasonCodes.sorted()))
1037:         }
1038: 
1039:     private fun decodeFutureFlexibility(
1040:         json: JSONObject,
1041:     ): FutureFlexibilityFeatures {
1042:         json.requireExactKeys("retainedRouteIds", "lostRouteIds", "reasonCodes")
1043:         return FutureFlexibilityFeatures(
1044:             retainedRouteIds = json.requiredStringSet("retainedRouteIds"),
1045:             lostRouteIds = json.requiredStringSet("lostRouteIds"),
1046:             reasonCodes = json.requiredStringSet("reasonCodes"),
1047:         )
1048:     }
1049: 
1050:     private fun encodePolicySnapshot(
1051:         snapshot: DecisionTracePolicySnapshot,
1052:     ): JSONObject =
1053:         when (snapshot) {
1054:             is DecisionTracePolicySnapshot.Ready -> JSONObject().apply {
1055:                 put("kind", "ready")
1056:                 put("policyVersion", snapshot.policyVersion.value)
1057:                 put("evaluations", JSONArray().apply {
1058:                     snapshot.evaluations.forEach { put(encodePolicyEvaluation(it)) }
1059:                 })
1060:                 put(
1061:                     "limitations",
1062:                     strings(snapshot.limitations.map { it.value }.sorted()),
1063:                 )
1064:             }
1065:             is DecisionTracePolicySnapshot.Deferred -> JSONObject().apply {
1066:                 put("kind", "deferred")
1067:                 put("policyVersion", snapshot.policyVersion.value)
1068:                 put("candidateIds", strings(snapshot.candidateIds))
1069:                 put("reasons", strings(snapshot.reasons.map { it.value }.sorted()))
1070:             }
1071:         }
1072: 
1073:     private fun decodePolicySnapshot(
1074:         json: JSONObject,
1075:     ): DecisionTracePolicySnapshot =
1076:         when (json.requiredNonBlankString("kind")) {
1077:             "ready" -> {
1078:                 json.requireExactKeys(
1079:                     "kind",
1080:                     "policyVersion",
1081:                     "evaluations",
1082:                     "limitations",
1083:                 )
1084:                 DecisionTracePolicySnapshot.Ready(
1085:                     policyVersion =
1086:                         PolicyVersion(json.requiredNonBlankString("policyVersion")),
1087:                     evaluations =
1088:                         json.requiredArray("evaluations")
1089:                             .mapObjects("evaluations", ::decodePolicyEvaluation),
1090:                     limitations =
1091:                         json.requiredStringSet("limitations")
1092:                             .mapTo(linkedSetOf()) { PolicyLimitationCode(it) },
1093:                 )
1094:             }
1095:             "deferred" -> {
1096:                 json.requireExactKeys(
1097:                     "kind",
1098:                     "policyVersion",
1099:                     "candidateIds",
1100:                     "reasons",
1101:                 )
1102:                 DecisionTracePolicySnapshot.Deferred(
1103:                     policyVersion =
1104:                         PolicyVersion(json.requiredNonBlankString("policyVersion")),
1105:                     candidateIds = json.requiredStringList("candidateIds"),
1106:                     reasons =
1107:                         json.requiredStringSet("reasons")
1108:                             .mapTo(linkedSetOf()) { PolicyDeferralCode(it) },
1109:                 )
1110:             }
1111:             else -> throw IllegalArgumentException(
1112:                 "Unsupported DecisionTrace policy snapshot kind.",
1113:             )
1114:         }
1115: 
1116:     private fun encodePolicyEvaluation(value: PolicyEvaluation): JSONObject =
1117:         JSONObject().apply {
1118:             put("candidateId", value.candidateId)
1119:             put("policyVersion", value.policyVersion.value)
1120:             put("disposition", value.disposition.name)
1121:             put(
1122:                 "rejectionReasons",
1123:                 strings(value.rejectionReasons.map { it.value }.sorted()),
1124:             )
1125:             put(
1126:                 "softPreferenceReasons",
1127:                 strings(value.softPreferenceReasons.map { it.value }.sorted()),
1128:             )
1129:             put("equivalenceState", encodeEquivalenceState(value.equivalenceState))
1130:         }
1131: 
1132:     private fun decodePolicyEvaluation(json: JSONObject): PolicyEvaluation {
1133:         json.requireExactKeys(
1134:             "candidateId",
1135:             "policyVersion",
1136:             "disposition",
1137:             "rejectionReasons",
1138:             "softPreferenceReasons",
1139:             "equivalenceState",
1140:         )
1141:         return PolicyEvaluation(
1142:             candidateId = json.requiredNonBlankString("candidateId"),
1143:             policyVersion =
1144:                 PolicyVersion(json.requiredNonBlankString("policyVersion")),
1145:             disposition = json.requiredEnum("disposition"),
1146:             rejectionReasons =
1147:                 json.requiredStringSet("rejectionReasons")
1148:                     .mapTo(linkedSetOf()) { PolicyReasonCode(it) },
1149:             softPreferenceReasons =
1150:                 json.requiredStringSet("softPreferenceReasons")
1151:                     .mapTo(linkedSetOf()) { PolicyReasonCode(it) },
1152:             equivalenceState =
1153:                 decodeEquivalenceState(json.requiredObject("equivalenceState")),
1154:         )
1155:     }
1156: 
1157:     private fun encodeEquivalenceState(
1158:         value: PolicyEquivalenceState,
1159:     ): JSONObject =
1160:         when (value) {
1161:             PolicyEquivalenceState.NotEvaluated -> JSONObject().apply {
1162:                 put("kind", "notEvaluated")
1163:             }
1164:             PolicyEquivalenceState.Unique -> JSONObject().apply {
1165:                 put("kind", "unique")
1166:             }
1167:             is PolicyEquivalenceState.Tied -> JSONObject().apply {
1168:                 put("kind", "tied")
1169:                 put("candidateIds", strings(value.candidateIds.sorted()))
1170:             }
1171:         }
1172: 
1173:     private fun decodeEquivalenceState(
1174:         json: JSONObject,
1175:     ): PolicyEquivalenceState =
1176:         when (json.requiredNonBlankString("kind")) {
1177:             "notEvaluated" -> {
1178:                 json.requireExactKeys("kind")
1179:                 PolicyEquivalenceState.NotEvaluated
1180:             }
1181:             "unique" -> {
1182:                 json.requireExactKeys("kind")
1183:                 PolicyEquivalenceState.Unique
1184:             }
1185:             "tied" -> {
1186:                 json.requireExactKeys("kind", "candidateIds")
1187:                 PolicyEquivalenceState.Tied(
1188:                     json.requiredStringSet("candidateIds"),
1189:                 )
1190:             }
1191:             else -> throw IllegalArgumentException(
1192:                 "Unsupported policy equivalence kind.",
1193:             )
1194:         }
1195: 
1196:     private fun encodePolicySelection(value: PolicySelection): JSONObject =
1197:         JSONObject().apply {
1198:             put("policyVersion", value.policyVersion.value)
1199:             put("candidateId", value.candidateId)
1200:             put("method", value.method.name)
1201:         }
1202: 
1203:     private fun decodePolicySelection(json: JSONObject): PolicySelection {
1204:         json.requireExactKeys("policyVersion", "candidateId", "method")
1205:         return PolicySelection(
1206:             policyVersion =
1207:                 PolicyVersion(json.requiredNonBlankString("policyVersion")),
1208:             candidateId = json.requiredNonBlankString("candidateId"),
1209:             method = json.requiredEnum("method"),
1210:         )
1211:     }
1212: 
1213:     private fun encodeActualChoice(
1214:         value: DecisionTraceActualChoice,
1215:     ): JSONObject =
1216:         when (value) {
1217:             DecisionTraceActualChoice.Pending -> JSONObject().apply {
1218:                 put("kind", "pending")
1219:             }
1220:             is DecisionTraceActualChoice.Committed -> JSONObject().apply {
1221:                 put("kind", "committed")
1222:                 put("candidateId", value.candidateId)
1223:                 put("source", value.source.name)
1224:                 put("manualOverride", value.manualOverride)
1225:                 put(
1226:                     "overrideReason",
1227:                     value.overrideReason?.let(::encodeOverrideReason)
1228:                         ?: JSONObject.NULL,
1229:                 )
1230:             }
1231:         }
1232: 
1233:     private fun decodeActualChoice(
1234:         json: JSONObject,
1235:     ): DecisionTraceActualChoice =
1236:         when (json.requiredNonBlankString("kind")) {
1237:             "pending" -> {
1238:                 json.requireExactKeys("kind")
1239:                 DecisionTraceActualChoice.Pending
1240:             }
1241:             "committed" -> {
1242:                 json.requireExactKeys(
1243:                     "kind",
1244:                     "candidateId",
1245:                     "source",
1246:                     "manualOverride",
1247:                     "overrideReason",
1248:                 )
1249:                 DecisionTraceActualChoice.Committed(
1250:                     candidateId = json.requiredNonBlankString("candidateId"),
1251:                     source =
1252:                         json.requiredEnum<InformationDecisionSource>("source"),
1253:                     manualOverride = json.requiredBoolean("manualOverride"),
1254:                     overrideReason =
1255:                         json.requiredNullableObject("overrideReason")
1256:                             ?.let(::decodeOverrideReason),
1257:                 )
1258:             }
1259:             else -> throw IllegalArgumentException(
1260:                 "Unsupported DecisionTrace actual-choice kind.",
1261:             )
1262:         }
1263: 
1264:     private fun encodeOverrideReason(
1265:         value: DecisionTraceOverrideReason,
1266:     ): JSONObject =
1267:         JSONObject().apply {
1268:             put("code", value.code ?: JSONObject.NULL)
1269:             put("text", value.text ?: JSONObject.NULL)
1270:         }
1271: 
1272:     private fun decodeOverrideReason(
1273:         json: JSONObject,
1274:     ): DecisionTraceOverrideReason {
1275:         json.requireExactKeys("code", "text")
1276:         return DecisionTraceOverrideReason(
1277:             code = json.requiredNullableString("code"),
1278:             text = json.requiredNullableString("text"),
1279:         )
1280:     }
1281: 
1282:     private fun strategicWorldKeys(
1283:         values: Set<StrategicWorldKey>,
1284:     ): JSONArray =
1285:         JSONArray().apply {
1286:             values.sortedWith(
1287:                 compareBy<StrategicWorldKey>(
1288:                     { it.demonSeat },
1289:                     { it.minionSeats.joinToString(",") },
1290:                 ),
1291:             ).forEach { value ->
1292:                 put(JSONObject().apply {
1293:                     put("demonSeat", value.demonSeat)
1294:                     put("minionSeats", ints(value.minionSeats))
1295:                 })
1296:             }
1297:         }
1298: 
1299:     private fun JSONObject.requiredStrategicWorldKeySet(
1300:         key: String,
1301:     ): Set<StrategicWorldKey> =
1302:         requiredArray(key)
1303:             .mapObjects(key) { json ->
1304:                 json.requireExactKeys("demonSeat", "minionSeats")
1305:                 StrategicWorldKey(
1306:                     demonSeat = json.requiredInt("demonSeat"),
1307:                     minionSeats = json.requiredIntList("minionSeats"),
1308:                 )
1309:             }
1310:             .toSetStrict(key)
1311: 
1312:     private fun healthyRoutes(
1313:         values: Set<out HealthyInformationRouteRef>,
1314:     ): JSONArray =
1315:         JSONArray().apply {
1316:             values.sortedBy(::healthyRouteSortKey)
1317:                 .forEach { put(encodeHealthyRoute(it)) }
1318:         }
1319: 
1320:     private fun healthyRouteSortKey(
1321:         value: HealthyInformationRouteRef,
1322:     ): String =
1323:         when (value) {
1324:             is HealthyInformationRouteRef.HistoricalObservation ->
1325:                 listOf(
1326:                     "0",
1327:                     value.recordId,
1328:                     value.recipientSeat.toString(),
1329:                 ).joinToString("|")
1330:             is HealthyInformationRouteRef.CurrentCandidate ->
1331:                 listOf(
1332:                     "1",
1333:                     value.candidateId,
1334:                     value.recipientSeat.toString(),
1335:                 ).joinToString("|")
1336:         }
1337: 
1338:     private fun JSONObject.requiredHealthyRouteSet(
1339:         key: String,
1340:     ): Set<HealthyInformationRouteRef> =
1341:         requiredArray(key)
1342:             .mapObjects(key, ::decodeHealthyRoute)
1343:             .toSetStrict(key)
1344: 
1345:     private fun JSONObject.requiredHealthyHistoricalRouteSet(
1346:         key: String,
1347:     ): Set<HealthyInformationRouteRef.HistoricalObservation> =
1348:         requiredArray(key)
1349:             .mapObjects(key) { json ->
1350:                 val route = decodeHealthyRoute(json)
1351:                 require(route is HealthyInformationRouteRef.HistoricalObservation) {
1352:                     key + " may only contain historical healthy-information routes."
1353:                 }
1354:                 route
1355:             }
1356:             .toSetStrict(key)
1357: 
1358:     private fun exposureTargets(
1359:         values: Set<RoleFunctionExposureTargetRef>,
1360:     ): JSONArray =
1361:         JSONArray().apply {
1362:             values.sortedWith(
1363:                 compareBy<RoleFunctionExposureTargetRef>(
1364:                     { it.seat },
1365:                     { it.role.value },
1366:                     { it.recipientSeat },
1367:                     { it.capability.name },
1368:                 ),
1369:             ).forEach { put(encodeExposureTarget(it)) }
1370:         }
1371: 
1372:     private fun JSONObject.requiredExposureTargetSet(
1373:         key: String,
1374:     ): Set<RoleFunctionExposureTargetRef> =
1375:         requiredArray(key)
1376:             .mapObjects(key, ::decodeExposureTarget)
1377:             .toSetStrict(key)
1378: 
1379:     private fun strings(values: Iterable<String>): JSONArray =
1380:         JSONArray().apply { values.forEach(::put) }
1381: 
1382:     private fun ints(values: Iterable<Int>): JSONArray =
1383:         JSONArray().apply { values.forEach(::put) }
1384: 
1385:     private fun JSONObject.requireExactKeys(vararg expected: String) {
1386:         val actual = keys().asSequence().toSet()
1387:         val expectedSet = expected.toSet()
1388:         require(actual == expectedSet) {
1389:             "Persisted object keys mismatch. Expected " +
1390:                 expectedSet +
1391:                 " but found " +
1392:                 actual +
1393:                 "."
1394:         }
1395:     }
1396: 
1397:     private fun JSONObject.requiredObject(key: String): JSONObject {
1398:         require(has(key) && !isNull(key)) { "Missing required object '" + key + "'." }
1399:         return opt(key) as? JSONObject
1400:             ?: throw IllegalArgumentException(
1401:                 "Persisted '" + key + "' must be an object.",
1402:             )
1403:     }
1404: 
1405:     private fun JSONObject.requiredNullableObject(
1406:         key: String,
1407:     ): JSONObject? {
1408:         require(has(key)) { "Missing required nullable object '" + key + "'." }
1409:         if (isNull(key)) return null
1410:         return opt(key) as? JSONObject
1411:             ?: throw IllegalArgumentException(
1412:                 "Persisted '" + key + "' must be an object or null.",
1413:             )
1414:     }
1415: 
1416:     private fun JSONObject.requiredArray(key: String): JSONArray {
1417:         require(has(key) && !isNull(key)) { "Missing required array '" + key + "'." }
1418:         return opt(key) as? JSONArray
1419:             ?: throw IllegalArgumentException(
1420:                 "Persisted '" + key + "' must be an array.",
1421:             )
1422:     }
1423: 
1424:     private fun JSONObject.requiredNonBlankString(key: String): String {
1425:         val value = requiredString(key)
1426:         require(value.isNotBlank()) { "Persisted '" + key + "' cannot be blank." }
1427:         return value
1428:     }
1429: 
1430:     private fun JSONObject.requiredString(key: String): String {
1431:         require(has(key) && !isNull(key)) { "Missing required string '" + key + "'." }
1432:         return opt(key) as? String
1433:             ?: throw IllegalArgumentException(
1434:                 "Persisted '" + key + "' must be a string.",
1435:             )
1436:     }
1437: 
1438:     private fun JSONObject.requiredNullableString(
1439:         key: String,
1440:     ): String? {
1441:         require(has(key)) { "Missing required nullable string '" + key + "'." }
1442:         if (isNull(key)) return null
1443:         return (opt(key) as? String)
1444:             ?.also {
1445:                 require(it.isNotBlank()) {
1446:                     "Persisted '" + key + "' cannot be blank."
1447:                 }
1448:             }
1449:             ?: throw IllegalArgumentException(
1450:                 "Persisted '" + key + "' must be a string or null.",
1451:             )
1452:     }
1453: 
1454:     private fun JSONObject.requiredBoolean(key: String): Boolean {
1455:         require(has(key) && !isNull(key)) { "Missing required boolean '" + key + "'." }
1456:         return opt(key) as? Boolean
1457:             ?: throw IllegalArgumentException(
1458:                 "Persisted '" + key + "' must be a boolean.",
1459:             )
1460:     }
1461: 
1462:     private fun JSONObject.requiredInt(key: String): Int {
1463:         val value = requiredLong(key)
1464:         require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
1465:             "Persisted '" + key + "' is outside Int range."
1466:         }
1467:         return value.toInt()
1468:     }
1469: 
1470:     private fun JSONObject.requiredNullableInt(
1471:         key: String,
1472:     ): Int? {
1473:         require(has(key)) { "Missing required nullable integer '" + key + "'." }
1474:         if (isNull(key)) return null
1475:         val raw = opt(key)
1476:         require(raw is Byte || raw is Short || raw is Int || raw is Long) {
1477:             "Persisted '" + key + "' must be an integer or null."
1478:         }
1479:         val value = (raw as Number).toLong()
1480:         require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
1481:             "Persisted '" + key + "' is outside Int range."
1482:         }
1483:         return value.toInt()
1484:     }
1485: 
1486:     private fun JSONObject.requiredLong(key: String): Long {
1487:         require(has(key) && !isNull(key)) { "Missing required integer '" + key + "'." }
1488:         val raw = opt(key)
1489:         require(raw is Byte || raw is Short || raw is Int || raw is Long) {
1490:             "Persisted '" + key + "' must be an integer."
1491:         }
1492:         return (raw as Number).toLong()
1493:     }
1494: 
1495:     private fun JSONObject.requiredBigInteger(key: String): BigInteger {
1496:         val value = requiredNonBlankString(key)
1497:         return runCatching { BigInteger(value) }
1498:             .getOrElse {
1499:                 throw IllegalArgumentException(
1500:                     "Persisted '" + key + "' must be an integer string.",
1501:                     it,
1502:                 )
1503:             }
1504:     }
1505: 
1506:     private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(
1507:         key: String,
1508:     ): T {
1509:         val value = requiredNonBlankString(key)
1510:         return enumValues<T>().firstOrNull { it.name == value }
1511:             ?: throw IllegalArgumentException(
1512:                 "Persisted '" + key + "' has an unknown enum value '" + value + "'.",
1513:             )
1514:     }
1515: 
1516:     private fun JSONObject.requiredStringList(key: String): List<String> =
1517:         requiredArray(key)
1518:             .mapValues(key) { index, value ->
1519:                 (value as? String)
1520:                     ?.also {
1521:                         require(it.isNotBlank()) {
1522:                             "Persisted '" + key + "'[" + index + "] cannot be blank."
1523:                         }
1524:                     }
1525:                     ?: throw IllegalArgumentException(
1526:                         "Persisted '" + key + "'[" + index + "] must be a string.",
1527:                     )
1528:             }
1529:             .also { values ->
1530:                 require(values.distinct().size == values.size) {
1531:                     "Persisted '" + key + "' entries must be unique."
1532:                 }
1533:             }
1534: 
1535:     private fun JSONObject.requiredStringSet(key: String): Set<String> =
1536:         requiredStringList(key).toCollection(linkedSetOf())
1537: 
1538:     private fun JSONObject.requiredIntList(key: String): List<Int> =
1539:         requiredArray(key).mapValues(key) { index, value ->
1540:             require(value is Byte || value is Short || value is Int || value is Long) {
1541:                 "Persisted '" + key + "'[" + index + "] must be an integer."
1542:             }
1543:             val longValue = (value as Number).toLong()
1544:             require(longValue in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
1545:                 "Persisted '" + key + "'[" + index + "] is outside Int range."
1546:             }
1547:             longValue.toInt()
1548:         }
1549: 
1550:     private inline fun <reified T : Enum<T>> JSONObject.requiredEnumSet(
1551:         key: String,
1552:     ): Set<T> {
1553:         val rawValues = requiredStringList(key)
1554:         return rawValues.mapTo(linkedSetOf()) { raw ->
1555:             enumValues<T>().firstOrNull { it.name == raw }
1556:                 ?: throw IllegalArgumentException(
1557:                     "Persisted '" + key + "' has an unknown enum value '" + raw + "'.",
1558:                 )
1559:         }.also { values ->
1560:             require(values.size == rawValues.size) {
1561:                 "Persisted '" + key + "' entries must be unique."
1562:             }
1563:         }
1564:     }
1565: 
1566:     private inline fun <T> JSONArray.mapObjects(
1567:         label: String,
1568:         transform: (JSONObject) -> T,
1569:     ): List<T> =
1570:         buildList {
1571:             for (index in 0 until length()) {
1572:                 val value = opt(index) as? JSONObject
1573:                     ?: throw IllegalArgumentException(
1574:                         "Persisted '" + label + "'[" + index + "] must be an object.",
1575:                     )
1576:                 add(transform(value))
1577:             }
1578:         }
1579: 
1580:     private inline fun <T> JSONArray.mapValues(
1581:         label: String,
1582:         transform: (Int, Any?) -> T,
1583:     ): List<T> =
1584:         buildList {
1585:             for (index in 0 until length()) {
1586:                 add(transform(index, opt(index)))
1587:             }
1588:         }
1589: 
1590:     private fun <T> List<T>.toSetStrict(label: String): Set<T> {
1591:         require(distinct().size == size) {
1592:             "Persisted '" + label + "' entries must be unique."
1593:         }
1594:         return toCollection(linkedSetOf())
1595:     }
1596: }