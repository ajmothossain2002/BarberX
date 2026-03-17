import {
    Injectable,
    NotFoundException,
    ForbiddenException,
    ConflictException,
    BadRequestException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBookingDto, UpdateBookingDto } from './dto';
import { BookingStatus } from '@prisma/client';

@Injectable()
export class BookingsService {
    constructor(private prisma: PrismaService) { }

    async create(userId: string, dto: CreateBookingDto) {
        const { shopId, serviceId, startTime } = dto;

        // Fetch service to get duration
        const service = await this.prisma.service.findUnique({
            where: { id: serviceId },
            include: { shop: true },
        });
        if (!service) throw new NotFoundException('Service not found');
        if (service.shopId !== shopId) throw new BadRequestException('Service does not belong to this shop');

        const start = new Date(startTime);
        if (Number.isNaN(start.getTime())) {
            throw new BadRequestException('Invalid startTime');
        }
        if (start.getTime() < Date.now()) {
            throw new BadRequestException('startTime must be in the future');
        }
        const end = new Date(start.getTime() + service.duration * 60000);

        // Check for overlapping bookings in the same shop
        const overlapping = await this.prisma.booking.findFirst({
            where: {
                shopId,
                status: { not: BookingStatus.CANCELLED },
                startTime: { lt: end },
                endTime: { gt: start },
            },
        });
        if (overlapping) {
            throw new ConflictException('Time slot is already booked');
        }

        // Optional: check shop working hours – can be added later

        return this.prisma.booking.create({
            data: {
                userId,
                shopId,
                serviceId,
                startTime: start,
                endTime: end,
                status: BookingStatus.PENDING,
            },
            include: {
                user: { select: { name: true, email: true } },
                service: true,
                shop: { select: { name: true, address: true } },
            },
        });
    }

    async findAllForUser(userId: string) {
        return this.prisma.booking.findMany({
            where: { userId },
            include: {
                shop: { select: { name: true, address: true } },
                service: true,
            },
            orderBy: { startTime: 'desc' },
        });
    }

    async findAllForShop(shopId: string, userId: string) {
        // Verify user owns the shop
        const shop = await this.prisma.shop.findUnique({ where: { id: shopId } });
        if (!shop) throw new NotFoundException('Shop not found');
        if (shop.ownerId !== userId) throw new ForbiddenException('Not authorized');

        return this.prisma.booking.findMany({
            where: { shopId },
            include: {
                user: { select: { name: true, email: true, phone: true } },
                service: true,
            },
            orderBy: { startTime: 'desc' },
        });
    }

    async findOne(id: string) {
        const booking = await this.prisma.booking.findUnique({
            where: { id },
            include: {
                user: { select: { name: true, email: true, phone: true } },
                shop: true,
                service: true,
            },
        });
        if (!booking) throw new NotFoundException('Booking not found');
        return booking;
    }

    async updateStatus(id: string, userId: string, dto: UpdateBookingDto) {
        if (!dto.status) {
            throw new BadRequestException('status is required');
        }
        const booking = await this.prisma.booking.findUnique({
            where: { id },
            include: { shop: true },
        });
        if (!booking) throw new NotFoundException('Booking not found');

        // Only the shop owner can update status (confirm/cancel) or the user can cancel their own?
        // We'll allow the user to cancel their own pending booking, and shop owner to change any.
        const isOwner = booking.shop.ownerId === userId;
        const isCustomer = booking.userId === userId;

        if (dto.status === BookingStatus.CANCELLED && isCustomer && booking.status === BookingStatus.PENDING) {
            // customer can cancel only pending bookings
        } else if (!isOwner) {
            throw new ForbiddenException('Only the shop owner can change booking status');
        }

        return this.prisma.booking.update({
            where: { id },
            data: { status: dto.status },
        });
    }
}
