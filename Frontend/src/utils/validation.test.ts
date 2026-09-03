import { describe, expect, it } from "vitest";
import { validateDateRange, validateTransfer } from "./validation";

describe("validateTransfer", () => {
  it("accepts a positive amount between different wallets", () => {
    expect(
      validateTransfer("source", "destination", 10000)
    ).toBeNull();
  });

  it("rejects self transfer", () => {
    expect(
      validateTransfer("same", "same", 10000)
    ).toBe("Source and destination wallets must be different.");
  });

  it("rejects non-positive amounts", () => {
    expect(
      validateTransfer("source", "destination", 0)
    ).toBe("Amount must be a positive whole number of paise.");
  });
});

describe("validateDateRange", () => {
  it("rejects reversed ranges", () => {
    expect(
      validateDateRange("2026-09-03T10:00", "2026-09-02T10:00")
    ).toBe("The start date must be before the end date.");
  });
});