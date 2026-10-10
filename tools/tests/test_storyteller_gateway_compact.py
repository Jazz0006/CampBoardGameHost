"""Production compact whole-game response protocol: completely offline."""
from __future__ import annotations
import copy
import importlib.util
import json
from pathlib import Path
import unittest

TOOLS=Path(__file__).resolve().parents[1]
G=importlib.util.spec_from_file_location("shape_gateway",TOOLS/"storyteller_gateway.py")
gateway=importlib.util.module_from_spec(G)
G.loader.exec_module(gateway)
S=importlib.util.spec_from_file_location("shape_smoke",TOOLS/"prod_global_live_smoke.py")
smoke=importlib.util.module_from_spec(S)
S.loader.exec_module(smoke)

def upstream(result):
    return {"status":"completed","output":[{"type":"message","content":[
        {"type":"output_text","text":json.dumps(result,ensure_ascii=False)}
    ]}]}

class CompactProductionTests(unittest.TestCase):
    def setup(self):
        # Poisoner acts before Investigator. Later Chef receives the *actual*
        # previous Investigator observation, not earlier model preview.
        first=smoke.investigator_case([smoke.action(0,1)])
        observed=smoke.observation(1,3,"Investigator",
            {"type":"PAIR_INFORMATION","shownRoleId":"Poisoner","candidateSeats":[6,7]})
        second=smoke.chef_case([smoke.action(0,1),observed])
        third=smoke.fortune_teller_case()
        return (first,second,third)

    def test_identical_history_legal_domain_and_unmodified_full_contract(self):
        for case in self.setup():
            with self.subTest(case=case["identity"]["decisionId"]):
                allowed=gateway.validate_host_request(case)
                full=gateway.build_openai_request(case,"gpt-6-luna")
                compact=copy.deepcopy(case)
                compact["responseProfile"]=gateway.COMPACT_MEMO_PROFILE
                self.assertEqual(gateway.validate_host_request(compact),allowed)
                minimal=gateway.build_openai_request(compact,"gpt-6-luna")
                self.assertEqual(json.loads(full["input"]),case)
                self.assertEqual(json.loads(minimal["input"]),compact)
                self.assertEqual(minimal["model"],full["model"])
                self.assertNotIn("reasoning",minimal)
                self.assertFalse(minimal["store"])
                self.assertEqual(set(minimal["text"]["format"]["schema"]["properties"]),
                                 {"candidateId","planMemo"})
                self.assertEqual(full["text"]["format"]["schema"]["required"][0],"strategy")
                # Advisory memo can be passed to the next decision only as a
                # fallible model hint, never a new Host truth.
                compact["priorCompactMemo"]="上轮考虑投毒者周围的隐士登记歧义"
                self.assertEqual(gateway.validate_host_request(compact),allowed)
                self.assertEqual(json.loads(gateway.build_openai_request(
                    compact,"gpt-6-luna")["input"])["priorCompactMemo"],
                    compact["priorCompactMemo"])
                compact["priorCompactMemo"]="x"*201
                with self.assertRaises(ValueError):
                    gateway.validate_host_request(compact)
                compact["priorCompactMemo"]=False
                with self.assertRaises(ValueError):
                    gateway.validate_host_request(compact)

    def test_legality_strict_shape_and_no_pretend_strategy(self):
        for case in self.setup():
            legal=gateway.validate_host_request(case)
            valid=gateway.parse_compact_memo_response(
                upstream({"candidateId":sorted(legal)[0],"planMemo":"先对照实际投毒及已交付调查员信息；其余线索待出现。"}),legal
            )
            self.assertEqual(valid["responseProfile"],"COMPACT_MEMO_V1")
            self.assertEqual(valid["alternatives"],[])
            self.assertNotIn("strategy",valid)
            for bad in (
                {"candidateId":"not-legal","planMemo":"信息生态"},
                {"candidateId":sorted(legal)[0],"planMemo":""},
                {"candidateId":sorted(legal)[0],"planMemo":"x"*201},
                {"candidateId":sorted(legal)[0],"planMemo":"ok","strategy":{}},
            ):
                with self.assertRaises(ValueError):
                    gateway.parse_compact_memo_response(upstream(bad),legal)
            with self.assertRaises(ValueError):
                gateway.parse_compact_memo_response({"status":"incomplete","output":[]},legal)

    def test_profile_is_only_for_real_live_information_request(self):
        setup=smoke.investigator_case([smoke.action(0,1)])
        setup["responseProfile"]="UNRECOGNIZED"
        with self.assertRaises(ValueError):
            gateway.validate_host_request(setup)
        setup["responseProfile"]="COMPACT_MEMO_V1"
        setup["strategicPlanningScope"]="OTHER"
        with self.assertRaises(ValueError):
            gateway.validate_host_request(setup)

if __name__=="__main__":
    unittest.main()
