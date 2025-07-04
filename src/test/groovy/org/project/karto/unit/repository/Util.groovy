package org.project.karto.unit.repository

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.project.karto.domain.card.entities.GiftCard
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.infrastructure.repository.JDBCUserRepository
import org.project.karto.util.TestDataGenerator

@ApplicationScoped
class Util {

    @Inject
    JDBCCompanyRepository companyRepo

    @Inject
    JDBCUserRepository userRepo

    @Inject
    JDBCGiftCardRepository giftCardRepo

    UUID generateActivateAndSaveUser() {
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()
        user.enable()
        userRepo.save(user)
        user.id()
    }

    UUID generateActivateAndSaveCompany() {
        def company = TestDataGenerator.generateCompany()
        company.incrementCounter()
        company.enable()
        companyRepo.save(company)
        company.id()
    }

    GiftCard generateActivateAndSaveSelfBoughtGiftCard() {
        def card = TestDataGenerator.generateSelfBougthGiftCard(generateActivateAndSaveUser(),
                generateActivateAndSaveCompany())
        giftCardRepo.save(card)
        card.activate()
        giftCardRepo.update(card)
        card
    }
}
