import unittest
from ai_conversation_probe import OPENING, CONTINUATION, prepare, check_app_request


def request(schema_id):
    return {"model": "gpt-6-luna", "store": False, "instructions": "expert",
            "input": '{"schemaId":"' + schema_id + '"}',
            "text": {"format": {"type": "json_schema", "strict": True,
                                "schema": {"type": "object"}, "name": "test"}}}


class ProbeTest(unittest.TestCase):
    def test_opening_preserves_schema_and_is_stored(self):
        source = request(OPENING)
        req = prepare(source, "opening")
        self.assertTrue(req["store"])
        self.assertEqual(req["text"], source["text"])
        self.assertEqual(req["instructions"], source["instructions"])
        self.assertFalse(source["store"])

    def test_chain_requires_host_validation_and_anchor(self):
        with self.assertRaises(ValueError):
            prepare(request(CONTINUATION), "chain", previous="resp_12345678")
        value = prepare(request(CONTINUATION), "chain",
                        previous="resp_12345678", host_verified=True,
                        reasoning="all_turns")
        self.assertEqual(value["previous_response_id"], "resp_12345678")
        self.assertEqual(value["reasoning"]["context"], "all_turns")

    def test_baseline_remains_stateless(self):
        v = prepare(request(CONTINUATION), "baseline")
        self.assertFalse(v["store"])
        self.assertNotIn("previous_response_id", v)

    def test_invalid_legacy_or_wrong_model_is_rejected(self):
        with self.assertRaises(ValueError):
            check_app_request(request("botc.storyteller-provider-request"))
        v = request(OPENING)
        v["model"] = "gpt-6-sol"
        with self.assertRaises(ValueError):
            check_app_request(v)

    def test_delta_marked_experimental_and_chain_only(self):
        delta = {"schemaId": "experiment.host-delta"}
        with self.assertRaises(ValueError):
            prepare(request(CONTINUATION), "baseline", delta=delta)
        x = prepare(request(CONTINUATION), "chain", previous="resp_12345678",
                    host_verified=True, delta=delta)
        self.assertIn("experiment.host-delta", x["input"])


if __name__ == "__main__":
    unittest.main()
